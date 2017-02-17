package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Loryn Fontaine on 08/02/2017.
 * This scheduler's objective is to prepare to an eventual switch failure. To do that, we allocate one VM on 2
 * on a G4 host, and the other on a G5 host. By doing that, a failure on a switch to G4's will make half of the VM's
 * unavailable only, and not all of them.
 * Worst-case complexity: O(n). The worst case is the one in which the last host only is appropriate. So we do
 * at the most n operations.
 */
public class DisasterRecoveryVmAllocationPolicy extends VmAllocationPolicy {

    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;
    private boolean previousWasInG4 = false;

    public DisasterRecoveryVmAllocationPolicy(List<? extends Host> list) {
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
        for (Host host : this.getHostList()) {
            int mips = host.getTotalMips(); // Get full Mips of the host
                if ((previousWasInG4 ^ mips == 3720) // If mips corresponding to the type of host we want we allocate
                        && allocateHostForVm(vm, host)) {
                    previousWasInG4 = !previousWasInG4;
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
