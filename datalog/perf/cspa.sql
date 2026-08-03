CREATE TABLE Assign(x INTEGER NOT NULL, y INTEGER NOT NULL);
COPY Assign FROM 'Assign.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Dereference(x INTEGER NOT NULL, y INTEGER NOT NULL);
COPY Dereference FROM 'Dereference.csv' WITH (FORMAT 'csv', DELIMITER ',');

SELECT * FROM umbra.datalog($$
.use assign
.use dereference
.decl ValueFlow(x: number, y: number)
.decl MemoryAlias(x: number, y: number)
.decl ValueAlias(x: number, y: number)

ValueFlow(y, x) :- assign(y, x).
ValueFlow(x, y) :- assign(x, z), MemoryAlias(z, y).
ValueFlow(x, y) :- ValueFlow(x, z), ValueFlow(z, y).
MemoryAlias(x, w) :- dereference(y, x), ValueAlias(y, z), dereference(z, w).
ValueAlias(x, y) :- ValueFlow(z, x), ValueFlow(z, y).
ValueAlias(x, y) :- ValueFlow(z, x), MemoryAlias(z, w), ValueFlow(w, y).
ValueFlow(x, x) :- assign(x, y).
ValueFlow(x, x) :- assign(y, x).
MemoryAlias(x, x) :- assign(y, x).
MemoryAlias(x, x) :- assign(x, y).

.decl result(relation:symbol, tupcount:bigint)
result('ValueFlow', count : { ValueFlow(_, _) }).
result('MemoryAlias', count : { MemoryAlias(_, _) }).
result('ValueAlias', count : { ValueAlias(_, _) }).

.output result
$$);
