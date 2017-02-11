package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas HORY
 * @version 10/02/17.
 *
 */
public class EnergyEfficientVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;


    public EnergyEfficientVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
        sortByMips();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
        sortByMips();
    }

    private void sortByMips(){

        for(int i=0;i<getHostList().size();i++){
            for(int j=i+1;j<getHostList().size();j++){
                if(getHostList().get(i).getTotalMips()>getHostList().get(j).getTotalMips()){
                    Host save= getHostList().get(j);
                    getHostList().set(j,getHostList().get(i));
                    getHostList().set(i,save);
                }
            }

        }
    }
    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        //nous l'avons choisir la node la moins utilisées
        double selectedPercentUsed=1;

        for(Host h: this.getHostList()){
            // on verifie si le host peut accueillir notre vm
            if(allocateHostForVm(vm,h)){
                return true;
                /*   //calcul le pourcentage de MIPS utilisés  et affectation sur le plus faible
                double mipsavailable =h.getAvailableMips();
                double mipstotal = h.getTotalMips();
                double percentMipsUsed=1-(mipsavailable/mipstotal);

                double ramavailable= h.getRamProvisioner().getAvailableRam();
                double ramtotal=h.getRam();
                double percentRamUsed=1-(ramavailable/ramtotal);

                double percentUsed = percentMipsUsed+percentRamUsed/2;
                if(selectedPercentUsed>percentUsed) {
                    selectedHost = h;
                    selectedPercentUsed = percentUsed;
                }*/

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
    public void deallocateHostForVm(Vm vm) {
        Host host = getHost(vm);
        host.vmDestroy(vm);
        hoster.remove(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return hoster.get(vm);
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Vm vm: hoster.keySet()) {
            if (vm.getId() == vmId && vm.getUserId() == userId) {
                return hoster.get(vm);
            }
        }
        return null;
    }
}
