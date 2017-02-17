package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fhermeni2 on 16/11/2015.
 * This scheduler simply places each Vm to the first appropriate Host.
 * Our algorithm simply uses the HostList and tries to allocate on each host. Once one allocation succeeded,
 * then we return true to indicate it. There are much penalties because of the fails possible in such an algorithm.
 * Worst-case complexity: O(n). This case is the one in which the allocation fails until the last host
 */
public class NaiveVmAllocationPolicy extends VmAllocationPolicy {

    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public NaiveVmAllocationPolicy(List<? extends Host> list) {
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
        /**
         * Simple algorithm: goes through the hostlist and tries to allocate for each host
         */
        for (Host host : this.getHostList()) {
            if (allocateHostForVm(vm, host)) {
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
