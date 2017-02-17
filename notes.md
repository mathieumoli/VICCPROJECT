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
Incomes:    12398,59€
Penalties:  402,16€
Energy:     2645,63€
Revenue:    9350,80€

We see that there are a lot of penalties and energy fees, which is mostly due to the fact that we try the 
allocation on every host until we find one which is appropriate.

### Anti Affinity algorithm
For this  algorithm, the impact on the cluster hosting capacity is that more hosts are going to be used. 
The reason is that the available capacity of a host is no more the only criteria in order to allocate a VM.
Because of this, some hosts would have the capacity for one more VM, but the affinity criteria is preventing
that, and another host has to be used.

### Fault-tolerance for standalone VMs
The infrastructure load in that particular context is different from others because we have "real" allocations,
and "previewed" ones since we prepare a new allocation for VM's which id is multiple of 10. In that context,
the load of the infrastructure is higher because we consider the resources these "previewed" allocations are
going to use.


### Load Balancing
The algorithm performing the best in terms of reduction of SLA violations is the worstFit one. The reason is 
that we allocate VM's to the hosts with the most Mips and RAM. By doing this, the probability of violating
SLA because of a lack of available resources is lower than with the nextFit algorithm which only considers
the last host which allocated a vm.

### Performance Satisfaction
This algorithm is effective because we can see by executing it that there are no reported penalties, which
means that we didn't try to allocate a VM to a host with not enough capacity for it.

### Energy-efficient schedulers
This algorithm has to be the less consumer in energy. Here the result for each previous algorithm:

Energy-efficient: 2604,30€
Fault Tolerance: 2911,59€
Naive: 2645,63€
AntiAffinity: 2688,44€
DisasterRecovery: 2649,07€
nextFit: 2715,76€
worstFit: 2791,80€
noViolation: 2868,74€

## Greedy scheduler

For this one we needed a scheduler which doesn't consume too much energy and, in the same time, not violating too much the SLAs.
To do it, we simply order the node in decreasing order and try to put the VM in the list from the bigger node to the
smaller one. This algorithm is not very complex (more or less the complexity is n²). It is not the most energy saver 
(our energy one consumes 2604€ of electricity, the no violation one uses 2868 € & this one 2686€) but its penalties are not enormous too (31,57€ for the greedy and 
1413€ for the energy one) that's why it is a way to maximize revenue (9680€ for the greedy one, the noViolations one has 9529€ for the revenues and the energy one permits to earn 8380€).


## Advice on the project
This project has been very interesting since we could discover the problems related to allocation of vm's. Moreover,
being able to see the results and the impact of some criterias on the revenue is also a good point. By implementing
these algorithms which are different one from another, we can really put in practice what we saw during the lectures.
In conclusion, we can say that the lecture and the projects interested us.