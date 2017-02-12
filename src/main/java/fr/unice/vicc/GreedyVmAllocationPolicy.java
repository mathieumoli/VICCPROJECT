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
 */
public class GreedyVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public GreedyVmAllocationPolicy(List<? extends Host> list) {
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
                if(getHostList().get(i).getTotalMips()<getHostList().get(j).getTotalMips()){
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
