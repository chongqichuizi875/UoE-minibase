For Task 2:

I extract the join conditions in the initialization of the JoinOperator. 

1: For loop each comparison atom.

2: Get 2 terms and the operator of the comparison atom.

3: Since JoinOperator has its left and right child  (select operation or other join operator) --> There
    are always at least one select operator help deal with
    the comparisons of a variable and a constant like x>9.
    In my task2 code, I pass all the comparisons to each selector, then build left deep
    operator tree via join based on these selections. So it performs
    the same -> I can assert both terms as Variable.

4: Compute the index of term1 in both left and right child's terms, 
    and the same to term2. Now we get 4 indexes.

5: There will be 2 scenes. term1 in left child && term2 in 
    right child. Or term1 in right child && term2 in left child.
    We can neglect others because we can not solve that in this join stage.

6: According to the above 2 scenes, I use a map to store the list of 
    integer index as key, while the Comparison operator as value. So I can
    only iterate the map to fetch the corresponding column 
    of each tuple and execute the compare.

tips: Under the above 2 scenes, the key of map is a list. While list[0] corresponds to 
    tuple column index in left child, list[1] corresponds to right. 
    So the two opposite scenes will invert the comparison.
    e.g. R(x,y),S(u,v),x<u is the same as u<x. So I maintain an
    inverted map to invert the comparison operators, which easily solved the problem.


For Task 3:

1: I want to minimize the join. e.g.
    Q(x,y,z):-R(x,y),R(u,v),S(z,w) I can replace it as
    Q(x,y,z):-R(x,y),S(z,w). However, I will maintain the join order (for R&S&T != R&T&S).
    And distinct relation will exist at least once.


2: I want to assign Comparison atoms wisely to its corresponding relation atom.
    e.g. x<5 will assign only to R(x,y) and z = 'adbs' only to S(z,w). Thus, we can reduce a lot 
    of selecting time when the original query has a lot of comparisons.

3: I want to smplify variables where possible. e.g. Q(x,y,z):-
    R(x,y,z), R(u,v,w), w=7 can transform to Q(x,y,z):-
    R(x,y,z), z=7.

Above is my central idea of optimisation. 

The reason is that: 

1: simplify one relation will save at least one scan, select, join time cost,
    and reduce the self cross product causing large intermediate tuples.

2: Instead of every select operator going over all the comparisons, assigning them 
    wisely at the initialization stage can save a lot of time, but do not cause
    more space usage. For we will assign the corresponding comparisons to
    its relation. Only a fraction of the relevant Comparison atoms can be 
    filtered to achieve the same result when queried 
    (the amount of data is reduced by the same amount due to 
    the query), but a lot less useless judgements and comparisons are made, 
    saving time.

The realization of the optimization is in QueryEvaluator.java. And called 
inside the QueryPlan.java(line 50). In the meantime, QueryPlan.java(line 51) is the 
default building of the operation tree, without cutting variables/relations and 
automatic assignment.