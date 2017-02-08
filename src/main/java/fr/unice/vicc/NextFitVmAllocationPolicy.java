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
        // On parcourt la liste depuis la position de la dernière allocation jusqu'a la fin de la liste
        for (int i = indexLastAllocation; i < this.getHostList().size(); i++) {
            if (allocateHostForVm(vm, this.getHostList().get(i))) {
                indexLastAllocation = i; // Mise a jour de l'index
                return true;
            }
        }
        // Si l'on arrive ici alors on n'a pas trouvé de host pouvant accepter la VM,
        // donc on reparcourt du début de la liste jusqu'a l'index de la dernière allocation
        for (int i = 0; i < indexLastAllocation; i++) {
            if (allocateHostForVm(vm, this.getHostList().get(i))) {
                indexLastAllocation = i; // Mise a jour de l'id
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
