package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mathieumoli on 4/02/2015.
 */
public class FaultToleranceVmAllocationPolicy2 extends VmAllocationPolicy {

    private Map<Vm, Host> hoster;
    private Map<Integer, Host> used;
    private Map<Integer, Double> usedCPU;
    private Map<Integer, Integer> usedRAM;


    public FaultToleranceVmAllocationPolicy2(List<? extends Host> list) {
        super(list);
        this.hoster = new HashMap<Vm, Host>();
        used = new HashMap<Integer, Host>();
        usedCPU = new HashMap<Integer, Double>();
        usedRAM = new HashMap<Integer, Integer>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        this.hoster = new HashMap<Vm, Host>();
    }


    @Override
    public boolean allocateHostForVm(Vm vm) {
        System.out.println(vm.getId());
        Host h = getaHostForVM(vm);
        int id = vm.getId();
        if (h != null ) {
            if(allocateHostForVm(vm,h)) {
                affectUsed(h, vm);

                //si multiple de 10 on reserve la place
                if ((id % 10) == 0) {
                    Host secondHost = getaSecondHostForVM(vm, h);
                    //si on trouve pas de second host on detruit
                    if (secondHost == null) {
                        deallocateHostForVm(vm);
                        unlockHostRessources(h, vm);
                        return false;
                    }
                        affectUsed(secondHost, vm);

                }
                return true;
            }
        }
        return false;
    }



    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.vmCreate(vm)) {
            hoster.put(vm, host);
            return true;
        }

        return false;

    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getHost(vm);
        host.vmDestroy(vm);
        hoster.remove(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return this.hoster.get(vm);
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Map.Entry<Vm, Host> vh : this.hoster.entrySet()) {
            if (vh.getKey().getUserId() == userId) {
                if (vh.getKey().getId() == vmId) {
                    return vh.getValue();
                }
            }
        }
        return null;
    }

    private Host getaHostForVM(Vm vm) {
        double mipsForVM = vm.getMips();
        int ramForVM = vm.getRam();
        int ram = 0;
        double mips=0;
        for(Host h : getHostList()) {
            //si deja utilisé
            if(used.get(h.getId())!=null) {
                ram = usedRAM.get(h.getId());
                mips = usedCPU.get(h.getId());
            }
            double hMips=h.getTotalMips();
            int hRam=h.getRam();
            if(((hMips - mips) >= mipsForVM && ((hRam - ram) >= ramForVM))) {
                return h;
            }
        }

        return null;
    }

    private Host getaSecondHostForVM(Vm vm,Host h){
        double mipsForVM = vm.getMips();
        int ramForVM = vm.getRam();
        int ram =0;
        double mips=0;
        for(Host candidateHost : getHostList()) {
            // si on reserve sur un host il faut qu'il soit different du premier
            if(candidateHost.getId()!=h.getId()) {
                //si deja utilisé
                if (used.get(h.getId()) != null) {
                    ram = usedRAM.get(h.getId());
                    mips = usedCPU.get(h.getId());
                }
                double hMips=h.getTotalMips();
                int hRam=h.getRam();
                if (((hMips - mips) > mipsForVM
                        && ((hRam - ram) > ramForVM))) {
                    return h;
                }
            }
        }

        return null;

    }

    private void affectUsed(Host h, Vm vm) {
        //si deja present
        if(used.containsKey(h.getId())) {
            //on met a jour seulement
            addUsed(h.getId(),vm.getMips(),vm.getRam());
        }else {
            //on cree l'utilisation
            used.put(h.getId(), h);
            usedCPU.put(h.getId(), vm.getMips());
            usedRAM.put(h.getId(), vm.getRam());
        }

    }
    private void addUsed(Integer id, double cpu, int ram) {
        //si l'host a deja des VMs allouées ou reservées

        usedCPU.put(id,usedCPU.get(id)+cpu);
        usedRAM.put(id,usedRAM.get(id)+ram);

    }

    private void removeUsed(Integer id, double cpu, int ram) {
        //si l'host a deja des VMs allouées ou reservées

        usedCPU.put(id,usedCPU.get(id)-cpu);
        usedRAM.put(id,usedRAM.get(id)-ram);

    }

    private void unlockHostRessources(Host h, Vm vm) {
        if(used.containsKey(h.getId())) {
            removeUsed(h.getId(), vm.getMips(), vm.getRam());
            //si pleine capacité on supprime des utilisés
            if(usedCPU.get(h.getId())==0 && usedRAM.get(h.getId())==0){
                used.remove(h.getId());
        }
       }
}

}