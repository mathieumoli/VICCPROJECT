package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.*;

/**
 * @author Nicolas HORY
 * @version 08/02/17.
 */
public class WorstFitVmAllocationPolicy extends VmAllocationPolicy{
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;
    private Map<Host, List<Double>> mipsAndRamHosts;

    public WorstFitVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
        mipsAndRamHosts = new HashMap<>();
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
        reinitializeMipsRam();
        double maxMips = 0.0;
        double maxRam = 0.0;
        Host maxHost = null;
        while (true) {
            for (Host host : mipsAndRamHosts.keySet()) {
                if (mipsAndRamHosts.get(host).get(0) > maxMips && mipsAndRamHosts.get(host).get(1) > maxRam) {
                    maxHost = host;
                }
            }
            if (allocateHostForVm(vm, maxHost)) {
                return true;
            } else {
                mipsAndRamHosts.remove(maxHost);
            }
        }
    }

    /**
     * Reinitialize the map with Mips and Ram values
     * @return
     */
    private void reinitializeMipsRam() {
        mipsAndRamHosts = new HashMap<>();
        for (Host host : this.getHostList()) {
            List<Double> valuesHost = new ArrayList<>();
            valuesHost.add(host.getAvailableMips());
            valuesHost.add((double) host.getRamProvisioner().getAvailableRam());
            mipsAndRamHosts.put(host, valuesHost);
        }
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
