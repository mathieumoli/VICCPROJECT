# Notes about the project

## The team

- Loryn Fontaine: loryn.fontaine@etu.unice.fr
- Nicolas Hory: nicolas.hory@etu.unice.fr
- Mathieu Molinengo: mathieu.molinengo@etu.unice.fr

## Comments

### Naive scheduler
Code: NaiveVmAllocationPolicy.java  
This scheduler is the one with the most simple algorithm. Indeed, we just allocate the vm to the first appropriate
host. We didn't have any problem implementing this scheduler since it's very basic to go through a list and call
the method allocateHostForVM on each host of this list until we succeed in allocating.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  402,16€
* Energy:     2645,63€
* Revenue:    9350,80€

We see that there are a lot of penalties and energy fees, which is mostly due to the fact that we try the 
allocation on every host until we find one which is appropriate.

### Anti Affinity algorithm
Code: AntiAffinityVmAllocationPolicy  
For this  algorithm, the impact on the cluster hosting capacity is that more hosts are going to be used. 
The reason is that the available capacity of a host is no more the only criteria in order to allocate a VM.
Because of this, some hosts would have the capacity for one more VM, but the affinity criteria is preventing
that, and another host has to be used.
We didn't have any problem implementing this algorithm since it was not hard to get the VM's of a host and to
check if they had an in in the same hundred as the one to allocate (simply used the modulo)

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  200,95€
* Energy:     2688,44€
* Revenue:    9509,21€

We can see that there are more energy fees, which is tending to confirm that more hosts are used. However, there are 
less penalties because less allocation are tried (since we are more selective before trying an allocation).

### Disaster Recovery
Code: DisasterRecoveryVmAllocationPolicy.java  
For this algorithm, we just added a boolean to know to which type of host we wanted to allocate (G4 or G5).
Then for the allocation we just checked if the current host was appropriate (had the good type), and tried to allocate.
The only problem we had is quite small, since it was taking in account the Mips and the type of host, but we did 
it by using a XOR.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  2223,24€
* Energy:     2649,07€
* Revenue:    7526,28€

We see that with this algorithm the penalties are huge. This is due to the fact that we are more selective when 
allocating (by taking in account G4 and G5 only), and so for each VM there are less chances of finding the
type of host we want and having enough resources available.

### Fault-tolerance for standalone VMs
Code: FaultToleranceVmAllocationPolicy.java  
The infrastructure load in that particular context is different from others because we have "real" allocations,
and "planned" ones since we prepare a new allocation for VM's which id is multiple of 10. In that context,
the load of the infrastructure is higher because we consider the resources these "planned" allocations are
going to use.

We had problems implementing this algorithm, mainly considering the fact of allocating and then finding another
host which had enough CPU and Mips for the VM. However, we succeeded in doing this by storing available CPU
and Mips of each host in maps, updating it on each allocation.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  161,72€
* Energy:     2911,59€
* Revenue:    9325,28€

We see we are using more energy, but have less penalties.

### Load Balancing
Code: NextFitVmAllocationPolicy.java & WorstFitVmAllocationPolicy.java  

#### NextFit
This algorithm is quite simple. We try to allocate until we find an appropriate host. The only difference
with the naive algorithm is that for next allocation we start from the last allocated host, and not from
the beginning of the list. To do that, we just use a global variable storing the index of the last allocated
host in the list. Then we use this index for the next research. We didn't have any problem implementing this
algorithm.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  346,75€
* Energy:     2715,76€
* Revenue:    9336,07€

When we compare these results to the one of the naive scheduler, we see we have less penalties, which is quite
logic since naive is starting from the beginning and tries to allocate to hosts which we probably already used,
while with nextFit this probability is lower.

#### WorstFit
This algorithm needs to know which hosts have the most resources. To do that, we created a map storing for each
host his Mips and Ram. Then when we try to allocate we browse this map, and get the host with the most Ram and Mips.
If the allocation fails, we remove this host from the map and start again. We had some problems when creating
the algorithm because we were not sure how to manage to find the host with the most resources in case of a first 
fail, but finally we used an infinite loop in which we remove at each step the host for which the allocation failed.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  115,83€
* Energy:     2789,02€
* Revenue:    9493,74€

We see that we don't have much penalties, which is logic since we try to allocate to hosts with most resources.


#### Comparison of the results between NextFit and WorstFit
The algorithm performing the best in terms of reduction of SLA violations is the worstFit one. The reason is 
that we allocate VM's to the hosts with the most Mips and RAM. By doing this, the probability of violating
SLA because of a lack of available resources is lower than with the nextFit algorithm which only considers
the last host which allocated a vm.

### Performance Satisfaction
Code: NoViolationsVmAllocationPolicy.java  
This algorithm is effective because we can see by executing it that there are no reported penalties, which
means that we didn't try to allocate a VM to a host with not enough capacity for it. To manage that, we used
the method isSuitable on a host before allocating, which means that the allocation won't be tried if the host
doesn't have enough free resources, and "solves" the problem of penalties. Since the modification is really simple
compared to other algorithms, we didn't have any problem implementing it.

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  0,00€
* Energy:     2868,74€
* Revenue:    9529,85€

We see that the revenue is higher than for other algorithms because of the penalties which is null.

### Energy-efficient schedulers
Code: EnergyEfficientVmAllocationPolicy.java  
This algorithm has to be the less consumer in energy. Here are the results for each previous algorithm:

* Fault Tolerance: 2911,59€  
* Naive: 2645,63€  
* AntiAffinity: 2688,44€  
* DisasterRecovery: 2649,07€  
* NextFit: 2715,76€  
* WorstFit: 2791,80€  
* NoViolation: 2868,74€  

We chose to sort the hosts by mips values, in increasing order. By doing that, the allocations will start 
in hosts with the less Mips, meaning that we are going to minimize the average resource used per host and so the energy fees.
The main thing to do for this algorithm was the sort, which is an algorithm we already saw during our scolarity
so we didn't have much problem to implement it.

The results for this algorithm in a simulation on all days are: 
* Incomes:    12398,59€
* Penalties:  1413,50€
* Energy:     2604,30€
* Revenue:    8380,79€

We see that the energy costs is lower than the ones we obtained for each other algorithm, showing the efficiency
of our algorithm. However, the penalties are really important, probably because once a host is full we still
try to allocate, and so on.


## Greedy scheduler
Code: GreedyVmAllocationVmAllocationPolicy.java  
For this one we needed a scheduler which doesn't consume too much energy and, in the same time, not violating
too much the SLAs. To do it, we simply order the nodes in decreasing order and try to put the VM in the list
from the bigger node to the smaller one. This algorithm is not very complex (more or less the complexity is n²). 

Results for a simulation on all days:
* Incomes:    12398,59€
* Penalties:  31,57€
* Energy:     2686,64€
* Revenue:    9680,38€

We can see that it is not the most energy saver (our energy one consumes 2604€ of electricity, the no violation
one uses 2868 € & this one 2686€) but its penalties are not enormous too (31,57€ for the greedy and 1413€ for
the energy one) that's why it is a way to maximize revenue (9680€ for the greedy one, the noViolations one 
has 9529€ for the revenues and the energy one permits to earn 8380€).



## Advice on the project
This project has been very interesting since we could discover the problems related to allocation of vm's. Moreover,
being able to see the results and the impact of some criterias on the revenue is also a good point. By implementing
these algorithms which are different one from another, we can really put in practice what we saw during the lectures.
We didn't have big problems during our implementations since we are used to programming, using maps and lists in Java.
Since they are the main things we used, the majority of what we did didn't fail and we succeeded in implementing
our algorithms quite fast, even if some reflection was needed concerning how to achieve the objectives of some algorithms, like
for the energy efficient one.
In conclusion, we can say that the lecture and the projects interested us.