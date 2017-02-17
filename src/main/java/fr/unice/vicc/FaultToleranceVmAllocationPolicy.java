package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mathieu MOLINENGO, corrected by Nicolas HORY
 * @version 04/02/2015.
 * This scheduler objective is to ensure the fault tolerance to 1 node failure for all the VM having an id
 * that is a multiple of 10. This implementation allocates "normally", and then searches for another host
 * which has enough resources for an eventual replicate.
 * Worst-case complexity: O(n) since we do maximum two operations/host, which is linear.
 */
public class FaultToleranceVmAllocationPolicy extends VmAllocationPolicy {

    private Map<Vm, Host> hoster;
    private Map<Integer, Host> used;
    private Map<Integer, Double> usedCPU;
    private Map<Integer, Integer> usedRAM;


    public FaultToleranceVmAllocationPolicy(List<? extends Host> list) {
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
        Host h = getaHostForVM(vm, null);
        int id = vm.getId();
        if (h != null ) {
            if(allocateHostForVm(vm,h)) {
                affectUsed(h, vm);
                //if multiple of 10 we reserve space
                if ((id % 10) == 0) {
                    Host secondHost = getaHostForVM(vm, h);
                    //if we don't find a second host we destroy
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

    /**
     * Cherche un host pour une VM
     * @param vm la VM pour laquelle on cherche un host
     * @param host l'host auquel la VM a été allouée, vaut null si c'est la première recherche
     * @return
     */
    private Host getaHostForVM(Vm vm, Host host) {
        double mipsForVM = vm.getMips();
        int ramForVM = vm.getRam();
        int ram = 0;
        double mips=0;
        for(Host h : getHostList()) {
            // If host is null then first search, else check the id is different of the allocated host's one
            if (host == null || host.getId() != h.getId()) {
                //if already used
                if (used.get(h.getId()) != null) {
                    ram = usedRAM.get(h.getId());
                    mips = usedCPU.get(h.getId());
                }
                double hMips = h.getTotalMips();
                int hRam = h.getRam();
                if (used.get(h.getId()) == null || ((hMips - mips) >= mipsForVM && ((hRam - ram) >= ramForVM))) {
                    return h;
                }
            }
        }

        return null;
    }

    private void affectUsed(Host h, Vm vm) {
        //if already present
        if(used.containsKey(h.getId())) {
            //just update
            addUsed(h.getId(),vm.getMips(),vm.getRam());
        }else {
            //create in the map
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