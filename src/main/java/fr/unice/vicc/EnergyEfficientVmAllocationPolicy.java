package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mathieu MOLINENGO
 * @version 10/02/17.
 * This scheduler aims at reducing energy costs. To do that, we chose to sort the hostlist according to mips
 * values of hosts, in decreasing order.
 * Worst-case complexity: O(n²) because the sort has this complexity, and the allocation is in O(n)
 */
public class EnergyEfficientVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public EnergyEfficientVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
        sortByMips(); // sort when construct the scheduler
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
        sortByMips();
    }

    /**
     * Sort the hostlist in increasing order according to mips value
     */
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
        for(Host h: this.getHostList()){
            // we check if the host can allocate our VM. Since the list is sorted, no more treatment needed
            if(allocateHostForVm(vm,h)){
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
