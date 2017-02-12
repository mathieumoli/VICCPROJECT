# Notes about the project

## The team

- Loryn Fontaine: loryn.fontaine@etu.unice.fr
- Nicolas Hory: nicolas.hory@etu.unice.fr
- Mathieu Molinengo: mathieu.molinengo@etu.unice.fr

## Comments

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

For this one we needed a scheduler which doesn't consume not to much energy and, in the same time, not violating too much the SLAs.
To do it we simply order the node in descreasing order and try to put the VM in the list from the bigger node to the
smaller one. This algorithm is not very complex (more or less the complexity is n² ). It is not the most energy saver 
(our energy one consumes 2604€ of electricity, the no violation one uses 2868 € & this one 2686€) but its penalties are not enormous too (31,57€ for the greedy and 
1413€ for the energy one) that's why it is a way to maximize revenue (9680€ for the greedy one, the noViolations one has 9529€ for the revenues and the energy one permits to earn 8380€).