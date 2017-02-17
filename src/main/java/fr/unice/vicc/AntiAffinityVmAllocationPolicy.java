package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nicolashory on 16/11/2015.
 * This scheduler places the Vms with regards to their affinity. In practice, all Vms with an id between [0-99]
 * must be on distinct nodes, the same with Vms having an id between [100-199], [200-299].
 * Our algorithm used the HostList, checks the VM's on each host, and if there is no VM with an id in the same hundred
 * as the VM to allocate, then try the allocation.
 * Worst-case complexity: O(n). This case is the one in which the allocation fails until the last host.
 */
public class AntiAffinityVmAllocationPolicy extends VmAllocationPolicy {

    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public AntiAffinityVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        // Go through the host list
        for (Host host : this.getHostList()) {
            List<Vm> VmSelect = host.getVmList(); // Get the vm's of the host
            boolean goodR=true; // Boolean indicating if there is a VM with an id in same hundred as the one to allocate
            for(Vm vmSelected: VmSelect) { // Verification on the id
                if (vmSelected.getId() / 100 == vm.getId() / 100) {
                    goodR=false;
                }
            }
            if (goodR && allocateHostForVm(vm, host)) { // Try to allocate if no vm in same hundred
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
        Host host = hoster.get(vm);
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
