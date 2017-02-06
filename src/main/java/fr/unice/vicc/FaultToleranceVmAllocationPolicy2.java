package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mathieumoli on 4/02/2015.
 */
public class FaultToleranceVmAllocationPolicy2 extends VmAllocationPolicy {

    private Map<Vm, Host> hoster;
    private Map<Integer, Host> used;
    private Map<Integer, Integer> cpuAvailable;
    private Map<Integer, Integer> ramAvailable;
    private Map<Integer, Long> storageAvailable;
    private Map<Vm, Host> reserved;


    public FaultToleranceVmAllocationPolicy2(List<? extends Host> list) {
        super(list);
        this.hoster = new HashMap<Vm, Host>();
        used = new HashMap<Integer, Host>();
        cpuAvailable = new HashMap<Integer, Integer>();
        ramAvailable = new HashMap<Integer, Integer>();
        storageAvailable = new HashMap<Integer, Long>();
        reserved = new HashMap<Vm, Host>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        this.hoster = new HashMap<Vm, Host>();
    }

    private boolean updateUsed(Integer id, double cpu, int ram, long storage) {
        //si l'host a deja des VMs allouées ou reservées

        if (used.containsKey(id)) {
            System.out.println("on l'a trouvé dans la map");
            System.out.println("cpu dispo:"+cpuAvailable.get(id));
            System.out.println("cpu demande"+cpu);

            System.out.println("ram dispo:"+ramAvailable.get(id));
            System.out.println("ram demande"+ram);

            System.out.println("storage dispo:"+storageAvailable.get(id));
            System.out.println("storage demande"+storage);

            if (cpuAvailable.get(id) >= cpu && ramAvailable.get(id) >= ram && storageAvailable.get(id) >= storage) {
                cpuAvailable.put(id, cpuAvailable.get(id) - (int) cpu);
                ramAvailable.put(id, ramAvailable.get(id) - ram);
                storageAvailable.put(id, storageAvailable.get(id) - storage);
                System.out.println("update realisé");
                return true;
            }

        }
        System.out.println("on a pas reussi");

        return false;
    }

    private boolean testUsed(Host h, double cpu, int ram, long storage) {
        //si l'host a deja des VMs allouées ou reservées
        if (h.getTotalMips() >= cpu && h.getRam() >= ram && h.getStorage() >= storage) {
            System.out.println("assez d'espace");
            return true;
        }
        System.out.println("need plus ");
        return false;
    }

    private boolean affectUsed(Host h, Vm vm) {
        used.put(h.getId(), h);
        cpuAvailable.put(h.getId(), h.getTotalMips());
        ramAvailable.put(h.getId(), h.getRam());
        storageAvailable.put(h.getId(), h.getStorage());
        return updateUsed(h.getId(), vm.getMips(), vm.getRam(), vm.getSize());

    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        for (Host h : getHostList()) {
            if (testUsed(h, vm.getMips(), vm.getRam(), vm.getSize())) {
                if (allocateHostForVm(vm, h)) {
                    return true;
                }
            }


        }return false;

    }

    private boolean bookSpace(Vm vm, Host h){
        for (Host host : getHostList()) {
            //on veut pas reserver dans l'host d'affectation
            if(host.getId()!=h.getId()){
                //on test si dans l'host il y a la place
                if (testUsed(host, vm.getMips(), vm.getRam(), vm.getSize())) {
                    System.out.println("je passe dans le bookspace");
                    //on regarde s'il y a deja des reservations dessus
                    if(used.containsKey(host.getId())){
                        System.out.println("la cle est contenu");

                        //si c'est le cas on fait juste un update
                        if(updateUsed(host.getId(), vm.getMips(), vm.getRam(), vm.getSize())){
                            reserved.put(vm,host);
                            System.out.println("je sors du bookSpace");
                            return true;
                        }
                    }else{
                        //sinon on l'ajoute et on update
                        if( affectUsed(host,vm)){
                            reserved.put(vm,host);
                            return true;
                        }

                    }
                }
            }
        }
        return false;
    }

    private boolean allocSpace(Vm vm, Host h){

        //on regarde s'il y a deja des reservations dessus
        if(used.containsKey(h.getId())){
            //si c'est le cas on fait juste un update
            if(updateUsed(h.getId(), vm.getMips(), vm.getRam(), vm.getSize())){
                System.out.println("je sors du allocSpace");

                return true;
            }
        }else{
            //sinon on l'ajoute et on update
            System.out.println("je sors du allocSpace par le else");
            return affectUsed(h,vm);
        }

        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if(vm.getId()%10==0){
            //on commence par reserver pour le back up
            if(bookSpace(vm,host)){
                //on alloue si on a book
                if(allocSpace(vm,host)){

                        if (host.vmCreate(vm)) {
                            hoster.put(vm, host);
                            return true;
                        }

                }

            }
            return false;
        }
        //si on a juste a allouer
            allocSpace(vm,host);
            if (host.vmCreate(vm)) {
                hoster.put(vm, host);
                return true;
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
        if(reserved.containsKey(vm)) {
            Host hostDeReserve = reserved.get(vm);
            updateUsed(hostDeReserve.getId(), -vm.getMips(), -vm.getRam(), -vm.getSize());
            reserved.remove(vm);
        }
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