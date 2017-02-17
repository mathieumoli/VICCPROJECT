package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas HORY
 * @version 08/02/17.
 * This scheduler uses the NextFit algorithm for the allocations. This means that the research of an appropriate
 * host starts from the last allocated one and not from the beginning.
 * Worst-case complexity: O(n) since the worst case is going through all the hosts.
 */
public class NextFitVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;
    private int indexLastAllocation;

    public NextFitVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
        indexLastAllocation = 0;
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
        //We start from the last allocation index until the end of the list
        for (int i = indexLastAllocation; i < this.getHostList().size(); i++) {
            if (allocateHostForVm(vm, this.getHostList().get(i))) {
                indexLastAllocation = i; // Update of the index
                return true;
            }
        }
        // If we get here then we didn't find an appropriate host to allocate the VM, so we start
        // from the beginning of the list until the index of last allocation
        for (int i = 0; i < indexLastAllocation; i++) {
            if (allocateHostForVm(vm, this.getHostList().get(i))) {
                indexLastAllocation = i; // Update of the index
                return true;
            }
        }
        return false; // If we get here then no host in the list is appropriate for the VM
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
