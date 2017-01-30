package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fhermeni2 on 16/11/2015.
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
        for (Host host : this.getHostList()) {
            List<Vm> VmSelect = host.getVmList();
            boolean goodR=true;
            for(Vm vmSelected: VmSelect) {
                if (vmSelected.getId() / 100 == vm.getId() / 100) {
                    goodR=false;
                }
            }
            if (goodR && allocateHostForVm(vm, host)) {
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
