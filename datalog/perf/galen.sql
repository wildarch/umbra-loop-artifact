CREATE TABLE P(X INT NOT NULL, Z INT NOT NULL);
COPY P FROM 'P.csv' (FORMAT 'csv');
CREATE TABLE Q(X INT NOT NULL, Y INT NOT NULL, Z INT NOT NULL);
COPY Q FROM 'Q.csv' (FORMAT 'csv');
CREATE TABLE R(R INT NOT NULL, P INT NOT NULL, E INT NOT NULL);
COPY R FROM 'R.csv' (FORMAT 'csv');
CREATE TABLE C(Y INT NOT NULL, Z INT NOT NULL, W INT NOT NULL);
COPY C FROM 'C.csv' (FORMAT 'csv');
CREATE TABLE U(R INT NOT NULL, Z INT NOT NULL, W INT NOT NULL);
COPY U FROM 'U.csv' (FORMAT 'csv');
CREATE TABLE S(R INT NOT NULL, P INT NOT NULL);
COPY S FROM 'S.csv' (FORMAT 'csv');

SELECT * FROM umbra.datalog($$
.use p
.use q
.use r
.use c
.use u
.use s
.decl OutP(X: number, Z: number)
.decl OutQ(X: number, Y: number, Z: number)

OutP(X, Z) :- p(X, Z).
OutQ(X, Y, Z) :- q(X, Y, Z).

OutP(?x,?z) :- OutP(?x,?y), OutP(?y,?z).
OutQ(?x,?r,?z) :- OutP(?x,?y), OutQ(?y,?r,?z).
OutP(?x,?z) :- OutP(?y,?w), u(?w,?r,?z), OutQ(?x,?r,?y).
OutP(?x,?z) :- c(?y,?w,?z),OutP(?x,?w), OutP(?x,?y).
OutQ(?x,?q,?z) :- OutQ(?x,?r,?z),s(?r,?q).
OutQ(?x,?e,?o) :- OutQ(?x,?y,?z),r(?y,?u,?e),OutQ(?z,?u,?o).

.decl result(relation:symbol, tupcount:bigint)
result('OutP', count : { OutP(_, _) }).
result('OutQ', count : { OutQ(_, _, _)}).
.output result
$$);
