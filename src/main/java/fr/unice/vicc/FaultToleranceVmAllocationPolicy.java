package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mathieumoli on 4/02/2015.
 *
 * NE MARCHE PAS ET LA COMME EXAMPLE POUR LE FAULT TOELERANCE 2
 */
public class FaultToleranceVmAllocationPolicy extends VmAllocationPolicy {

    private Map<Vm,Host> hoster;
    private Map<Integer,Host> used;
    private Map <Integer,Double> cpuAvailable;
    private Map <Integer, Integer> ramAvailable;
    private Map <Integer, Long> storageAvailable;


    public FaultToleranceVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        this.hoster = new HashMap<Vm,Host>();
        used=new HashMap<Integer,Host>();
        cpuAvailable= new HashMap<Integer,Double>();
        ramAvailable= new HashMap<Integer,Integer>();
        storageAvailable= new HashMap<Integer,Long>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        this.hoster = new HashMap<Vm,Host>();
    }

    private boolean updateUsed(Integer id, double cpu,int ram, long storage){
        //si l'host a deja des VMs allouées ou reservées
        if(used.get(id)!=null){
            if(cpuAvailable.get(id)>=cpu && ramAvailable.get(id)>=ram && storageAvailable.get(id)>=storage){
                cpuAvailable.put(id,cpuAvailable.get(id)-cpu);
                ramAvailable.put(id,ramAvailable.get(id)-ram);
                storageAvailable.put(id,storageAvailable.get(id)-storage);
                System.out.println("update realisé");
                return true;
            }

        }return false;
    }

    private boolean testUsed(Host h, double cpu,int ram, long storage){
        //si l'host a deja des VMs allouées ou reservées
        if(h.getAvailableMips()>=cpu && h.getRam()>=ram && h.getStorage()>=storage){
            System.out.println("assez d'espace");
            return true;
        }
        System.out.println("need plus ");
        return false;
    }
    private void affectUsed( Host h){
        used.put(h.getId(),h);
        cpuAvailable.put(h.getId(),h.getAvailableMips());
        ramAvailable.put(h.getId(),h.getRam());
        storageAvailable.put(h.getId(),h.getStorage());

    }
    @Override
    public boolean allocateHostForVm(Vm vm) {
        if ((vm.getId() % 10) == 0) {
            // j'alloue et je reserve
            if(bookSpace(vm)){
                if(allocSpace(vm)){
                    return true;
                }
               // updateUsed();
            }
            System.out.println("id de 10");
            return (allocSpace(vm)|| bookSpace(vm));
        }
        else{
            System.out.println("id pas de 10");
            //j'alloue seulement
            return allocSpace(vm);
        }



    }
    private boolean bookSpace(Vm vm){
        for (Host h : this.getHostList()) {
            // on cherche une place a reserver
            if ((used.get(h.getId())) != null) {
                if (testUsed(h, vm.getMips(), vm.getRam(), vm.getSize())) {
                    //on test la disponibilité et si c'est bon on break
                    if (updateUsed(h.getId(), vm.getMips(), vm.getRam(), vm.getSize())) {
                        System.out.println("je reserve la place sur un host deja utilisé");
                        return true;
                    }
                }

            }
            else{
                if(testUsed(h, vm.getMips(), vm.getRam(), vm.getSize())){
                    affectUsed(h);
                    updateUsed(h.getId(),vm.getMips(),vm.getRam(),vm.getSize());
                    System.out.println("je reserve la place sur un host nouveau");
                    return true;
                }

            }
        }
        return false;

    }

    private boolean allocSpace(Vm vm){
        for(Host h : this.getHostList()){
            //on regarde si l'host est utilisé
            if((used.get(h.getId()))!=null){

                if(testUsed(h,vm.getMips(),vm.getRam(),vm.getSize())){
                    //on test la disponibilité et si c'est bon on break
                    if(updateUsed(h.getId(),vm.getMips(),vm.getRam(),vm.getSize())) {
                        System.out.println("j'alloue sur un host deja utilisé");

                        if(allocateHostForVm(vm,h)){
                            return true;
                        }
                        updateUsed(h.getId(),-vm.getMips(),-vm.getRam(),-vm.getSize());
                        return false;

                    }
                }
            }else{
                //c'est la premiere fois pour l'host
                if(allocateHostForVm(vm,h)){
                    affectUsed(h);
                    System.out.println("j'alloue sur un host nouveau");

                    return updateUsed(h.getId(),vm.getMips(),vm.getRam(),vm.getSize());

                }


            }

        }
        return false;

    }
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if(host.isSuitableForVm(vm)){
            if (host.vmCreate(vm)) {
                hoster.put(vm, host);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getHost(vm);
        updateUsed(host.getId(),-vm.getMips(),-vm.getRam(),-vm.getSize());
        host.vmDestroy(vm);
        hoster.remove(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return this.hoster.get(vm);
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Map.Entry<Vm, Host> vh : this.hoster.entrySet()) {
            if (vh.getKey().getUserId() == userId) {
                if (vh.getKey().getId() == vmId) {
                    return vh.getValue();
                }
            }
        }
        return null;
    }

}