CREATE TABLE DirectSuperclass(class INTEGER NOT NULL, superclass INTEGER NOT NULL);
COPY DirectSuperclass FROM 'DirectSuperclass.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE DirectSuperinterface(ref INTEGER NOT NULL, interface INTEGER NOT NULL);
COPY DirectSuperinterface FROM 'DirectSuperinterface.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE MainClass(class INTEGER NOT NULL);
COPY MainClass FROM 'MainClass.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE FormalParam(index INTEGER NOT NULL, method INTEGER NOT NULL, var INTEGER NOT NULL);
COPY FormalParam FROM 'FormalParam.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE ComponentType(arrayType INTEGER NOT NULL, componentType INTEGER NOT NULL);
COPY ComponentType FROM 'ComponentType.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE AssignReturnValue(invocation INTEGER NOT NULL, to_ INTEGER NOT NULL);
COPY AssignReturnValue FROM 'AssignReturnValue.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE ActualParam(index INTEGER NOT NULL, invocation INTEGER NOT NULL, var INTEGER NOT NULL);
COPY ActualParam FROM 'ActualParam.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE Method_Modifier(mod INTEGER NOT NULL, method INTEGER NOT NULL);
COPY Method_Modifier FROM 'Method_Modifier.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE Var_Type(var INTEGER NOT NULL, type INTEGER NOT NULL);
COPY Var_Type FROM 'Var_Type.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE HeapAllocation_Type(heap INTEGER NOT NULL, type INTEGER NOT NULL);
COPY HeapAllocation_Type FROM 'HeapAllocation_Type.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _ClassType(class INTEGER NOT NULL);
COPY _ClassType FROM 'ClassType.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _ArrayType(arrayType INTEGER NOT NULL);
COPY _ArrayType FROM 'ArrayType.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _InterfaceType(interface INTEGER NOT NULL);
COPY _InterfaceType FROM 'InterfaceType.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _Var_DeclaringMethod(var INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _Var_DeclaringMethod FROM 'Var_DeclaringMethod.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _ApplicationClass(type INTEGER NOT NULL);
COPY _ApplicationClass FROM 'ApplicationClass.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _ThisVar(method INTEGER NOT NULL, var INTEGER NOT NULL);
COPY _ThisVar FROM 'ThisVar.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _NormalHeap(id INTEGER NOT NULL, type INTEGER NOT NULL);
COPY _NormalHeap FROM 'NormalHeap.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _StringConstant(id INTEGER NOT NULL);
COPY _StringConstant FROM 'StringConstant.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _AssignHeapAllocation(instruction INTEGER NOT NULL, index INTEGER NOT NULL, heap INTEGER NOT NULL, to_ INTEGER NOT NULL, inmethod INTEGER NOT NULL, linenumber INTEGER NOT NULL);
COPY _AssignHeapAllocation FROM 'AssignHeapAllocation.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _AssignLocal(instruction INTEGER NOT NULL, index INTEGER NOT NULL, from_ INTEGER NOT NULL, to_ INTEGER NOT NULL, inmethod INTEGER NOT NULL);
COPY _AssignLocal FROM 'AssignLocal.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _AssignCast(instruction INTEGER NOT NULL, index INTEGER NOT NULL, from_ INTEGER NOT NULL, to_ INTEGER NOT NULL, type INTEGER NOT NULL, inmethod INTEGER NOT NULL);
COPY _AssignCast FROM 'AssignCast.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _Field(signature INTEGER NOT NULL, declaringClass INTEGER NOT NULL, simplename INTEGER NOT NULL, type INTEGER NOT NULL);
COPY _Field FROM 'Field.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _StaticMethodInvocation(instruction INTEGER NOT NULL, index INTEGER NOT NULL, signature INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _StaticMethodInvocation FROM 'StaticMethodInvocation.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _SpecialMethodInvocation(instruction INTEGER NOT NULL, index INTEGER NOT NULL, signature INTEGER NOT NULL, base INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _SpecialMethodInvocation FROM 'SpecialMethodInvocation.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _VirtualMethodInvocation(instruction INTEGER NOT NULL, index INTEGER NOT NULL, signature INTEGER NOT NULL, base INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _VirtualMethodInvocation FROM 'VirtualMethodInvocation.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _Method(method INTEGER NOT NULL, simplename INTEGER NOT NULL, params INTEGER NOT NULL, declaringType INTEGER NOT NULL, returnType INTEGER NOT NULL, jvmDescriptor INTEGER NOT NULL, arity INTEGER NOT NULL);
COPY _Method FROM 'Method.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE Method_Descriptor(method INTEGER NOT NULL, descriptor INTEGER NOT NULL);
COPY Method_Descriptor FROM 'Method_Descriptor.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _StoreInstanceField(instruction INTEGER NOT NULL, index INTEGER NOT NULL, from_ INTEGER NOT NULL, base INTEGER NOT NULL, signature INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _StoreInstanceField FROM 'StoreInstanceField.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _LoadInstanceField(instruction INTEGER NOT NULL, index INTEGER NOT NULL, to_ INTEGER NOT NULL, base INTEGER NOT NULL, signature INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _LoadInstanceField FROM 'LoadInstanceField.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _StoreStaticField(instruction INTEGER NOT NULL, index INTEGER NOT NULL, from_ INTEGER NOT NULL, signature INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _StoreStaticField FROM 'StoreStaticField.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _LoadStaticField(instruction INTEGER NOT NULL, index INTEGER NOT NULL, to_ INTEGER NOT NULL, signature INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _LoadStaticField FROM 'LoadStaticField.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _StoreArrayIndex(instruction INTEGER NOT NULL, index INTEGER NOT NULL, from_ INTEGER NOT NULL, base INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _StoreArrayIndex FROM 'StoreArrayIndex.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _LoadArrayIndex(instruction INTEGER NOT NULL, index INTEGER NOT NULL, to_ INTEGER NOT NULL, base INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _LoadArrayIndex FROM 'LoadArrayIndex.csv' WITH (FORMAT 'csv', DELIMITER ',');
CREATE TABLE _Return(instruction INTEGER NOT NULL, index INTEGER NOT NULL, var INTEGER NOT NULL, method INTEGER NOT NULL);
COPY _Return FROM 'Return.csv' WITH (FORMAT 'csv', DELIMITER ',');

\set debug.joinordering.rightdeep 1
\set debug.joinorder i
\set debug.multiway d

SELECT * FROM umbra.datalog($$
.use directsuperclass
.use directsuperinterface
.use mainclass
.use formalparam
.use componenttype
.use assignreturnvalue
.use actualparam
.use method_modifier
.use var_type
.use heapallocation_type
.use _classtype
.use _arraytype
.use _interfacetype
.use _var_declaringmethod
.use _applicationclass
.use _thisvar
.use _normalheap
.use _stringconstant
.use _assignheapallocation
.use _assignlocal
.use _assigncast
.use _field
.use _staticmethodinvocation
.use _specialmethodinvocation
.use _virtualmethodinvocation
.use _method
.use method_descriptor
.use _storeinstancefield
.use _loadinstancefield
.use _storestaticfield
.use _loadstaticfield
.use _storearrayindex
.use _loadarrayindex
.use _return

.decl isType(t:number)
.decl isReferenceType(t:number)
.decl isArrayType(t:number)
.decl isClassType(t:number)
.decl isInterfaceType(t:number)
.decl ApplicationClass(ref:number)
.decl Field_DeclaringType(field:number, declaringClass:number)
.decl Method_DeclaringType(method:number, declaringType:number)
.decl Method_ReturnType(method:number, returnType:number) // redundant
.decl Method_SimpleName(method:number, simpleName:number)
.decl Method_Params(method:number, params:number) // redundant
.decl ThisVar(method:number, var:number)
.decl Var_DeclaringMethod(var:number, method:number)
.decl Instruction_Method(insn:number, inMethod:number)
.decl isVirtualMethodInvocation_Insn(insn:number)
.decl isStaticMethodInvocation_Insn(insn:number)
.decl FieldInstruction_Signature(insn:number, sign:number)
.decl LoadInstanceField_Base(insn:number, var:number)
.decl LoadInstanceField_To(insn:number, var:number)
.decl StoreInstanceField_From(insn:number, var:number)
.decl StoreInstanceField_Base(insn:number, var:number)
.decl LoadStaticField_To(insn:number, var:number)
.decl StoreStaticField_From(insn:number, var:number)
.decl LoadArrayIndex_Base(insn:number, var:number)
.decl LoadArrayIndex_To(insn:number, var:number)
.decl StoreArrayIndex_From(insn:number, var:number)
.decl StoreArrayIndex_Base(insn:number, var:number)
.decl AssignInstruction_To(insn:number, to:number)
.decl AssignCast_From(insn:number, from:number)
.decl AssignCast_Type(insn:number, type:number)
.decl AssignLocal_From(insn:number, from:number)
.decl AssignHeapAllocation_Heap(insn:number, heap:number)
.decl ReturnNonvoid_Var(return:number, var:number)
.decl MethodInvocation_Method(invocation:number, signature:number)
.decl VirtualMethodInvocation_Base(invocation:number, base:number)
.decl VirtualMethodInvocation_SimpleName(invocation:number, simplename:number)
.decl VirtualMethodInvocation_Descriptor(invocation:number, descriptor:number)
.decl SpecialMethodInvocation_Base(invocation:number, base:number)
.decl MethodInvocation_Base(invocation:number, base:number)

.decl LoadInstanceField(base:number, sig:number, to:number, inmethod:number)
.decl StoreInstanceField(from:number, base:number, signature:number, inmethod:number)
.decl LoadStaticField(sig:number, to:number, inmethod:number)
.decl StoreStaticField(from:number, signature:number, inmethod:number)
.decl LoadArrayIndex(base:number, to:number, inmethod:number)
.decl StoreArrayIndex(from:number, base:number, inmethod:number)
.decl AssignCast(type:number, from:number, to:number, inmethod:number)
.decl AssignLocal(from:number, to:number, inmethod:number)
.decl AssignHeapAllocation(heap:number, to:number, inmethod:number)
.decl ReturnVar(var:number, method:number)
.decl StaticMethodInvocation(invocation:number, signature:number, inmethod:number)


isType(class) :- _classtype(class).
isReferenceType(class) :- _classtype(class).
isClassType(class) :- _classtype(class).

isType(arrayType) :- _arraytype(arrayType).
isReferenceType(arrayType) :- _arraytype(arrayType).
isArrayType(arrayType) :- _arraytype(arrayType).

isType(interface) :- _interfacetype(interface).
isReferenceType(interface) :- _interfacetype(interface).
isInterfaceType(interface) :- _interfacetype(interface).

Var_DeclaringMethod(var, method) :- _var_declaringmethod(var, method).

isType(type) :- _applicationclass(type).
isReferenceType(type) :- _applicationclass(type).
ApplicationClass(type) :- _applicationclass(type).

ThisVar(method, var) :- _thisvar(method, var).

isType(type) :- _normalheap(_, type).

Instruction_Method(instruction, method) :-
  _assignheapallocation(instruction, index, heap, to, method, linenumber).
AssignInstruction_To(instruction, to) :-
  _assignheapallocation(instruction, index, heap, to, method, linenumber).
AssignHeapAllocation_Heap(instruction, heap) :-
  _assignheapallocation(instruction, index, heap, to, method, linenumber).

Instruction_Method(instruction, method) :-
  _assignlocal(instruction, index, from, to, method).
AssignLocal_From(instruction, from) :-
  _assignlocal(instruction, index, from, to, method).
AssignInstruction_To(instruction, to) :- 
  _assignlocal(instruction, index, from, to, method).

Instruction_Method(instruction, method) :-
  _assigncast(instruction, index, from, to, type, method).
AssignCast_Type(instruction, type) :-
  _assigncast(instruction, index, from, to, type, method).
AssignCast_From(instruction, from) :-
  _assigncast(instruction, index, from, to, type, method).
AssignInstruction_To(instruction, to) :- 
  _assigncast(instruction, index, from, to, type, method).

Field_DeclaringType(signature, declaringType) :- _field(signature, declaringType, _, _).

MethodInvocation_Base(invocation, base) :- VirtualMethodInvocation_Base(invocation, base).
MethodInvocation_Base(invocation, base) :- SpecialMethodInvocation_Base(invocation, base).



Instruction_Method(instruction, method) :-
  _staticmethodinvocation(instruction, index, signature, method).
isStaticMethodInvocation_Insn(instruction) :-
  _staticmethodinvocation(instruction, index, signature, method).
MethodInvocation_Method(instruction, signature) :- 
  _staticmethodinvocation(instruction, index, signature, method).

Instruction_Method(instruction, method) :-
  _specialmethodinvocation(instruction, index, signature, base, method).
SpecialMethodInvocation_Base(instruction, base) :-
  _specialmethodinvocation(instruction, index, signature, base, method).
MethodInvocation_Method(instruction, signature) :-
  _specialmethodinvocation(instruction, index, signature, base, method).

Instruction_Method(instruction, method) :-
  _virtualmethodinvocation(instruction, index, signature, base, method).
isVirtualMethodInvocation_Insn(instruction) :-
  _virtualmethodinvocation(instruction, index, signature, base, method).
VirtualMethodInvocation_Base(instruction, base) :-
  _virtualmethodinvocation(instruction, index, signature, base, method).
MethodInvocation_Method(instruction, signature) :-
  _virtualmethodinvocation(instruction, index, signature, base, method).

Method_SimpleName(method, simplename) :-
  _method(method, simplename, params, declaringType, returnType, jvmDescriptor, arity).
Method_DeclaringType(method, declaringType) :-
  _method(method, simplename, params, declaringType, returnType, jvmDescriptor, arity).

Instruction_Method(instruction, method) :-
  _storeinstancefield(instruction, index, from, base, signature, method).
FieldInstruction_Signature(instruction, signature) :-
  _storeinstancefield(instruction, index, from, base, signature, method).
StoreInstanceField_Base(instruction, base) :-
  _storeinstancefield(instruction, index, from, base, signature, method).
StoreInstanceField_From(instruction, from) :-
  _storeinstancefield(instruction, index, from, base, signature, method).

Instruction_Method(instruction, method) :-
  _loadinstancefield(instruction, index, to, base, signature, method).
FieldInstruction_Signature(instruction, signature) :-
  _loadinstancefield(instruction, index, to, base, signature, method).
LoadInstanceField_Base(instruction, base) :-
  _loadinstancefield(instruction, index, to, base, signature, method).
LoadInstanceField_To(instruction, to) :-
  _loadinstancefield(instruction, index, to, base, signature, method).

Instruction_Method(instruction, method) :-
  _storestaticfield(instruction, index, from, signature, method).
FieldInstruction_Signature(instruction, signature) :-
  _storestaticfield(instruction, index, from, signature, method).
StoreStaticField_From(instruction, from) :-
  _storestaticfield(instruction, index, from, signature, method).

Instruction_Method(instruction, method) :-
  _loadstaticfield(instruction, index, to, signature, method).
FieldInstruction_Signature(instruction, signature) :-
  _loadstaticfield(instruction, index, to, signature, method).
LoadStaticField_To(instruction, to) :-
  _loadstaticfield(instruction, index, to, signature, method).

Instruction_Method(instruction, method) :-
  _storearrayindex(instruction, index, from, base, method).
StoreArrayIndex_Base(instruction, base) :-
  _storearrayindex(instruction, index, from, base, method).
StoreArrayIndex_From(instruction, from) :-
  _storearrayindex(instruction, index, from, base, method).

Instruction_Method(instruction, method) :-
  _loadarrayindex(instruction, index, to, base, method).
LoadArrayIndex_Base(instruction, base) :-
  _loadarrayindex(instruction, index, to, base, method).
LoadArrayIndex_To(instruction, to) :-
  _loadarrayindex(instruction, index, to, base, method).

Instruction_Method(instruction, method) :-
  _return(instruction, index, var, method).
ReturnNonvoid_Var(instruction, var) :-
  _return(instruction, index, var, method).


LoadInstanceField(base, sig, to, inmethod) :-
  Instruction_Method(insn, inmethod),
  LoadInstanceField_Base(insn, base),
  FieldInstruction_Signature(insn, sig),
  LoadInstanceField_To(insn, to).
StoreInstanceField(from, base, sig, inmethod) :-
  Instruction_Method(insn, inmethod),
  StoreInstanceField_From(insn, from),
  StoreInstanceField_Base(insn, base),
  FieldInstruction_Signature(insn, sig).
LoadStaticField(sig, to, inmethod) :-
  Instruction_Method(insn, inmethod),
  FieldInstruction_Signature(insn, sig),
  LoadStaticField_To(insn, to).
StoreStaticField(from, sig, inmethod) :-
  Instruction_Method(insn, inmethod),
  StoreStaticField_From(insn, from),
  FieldInstruction_Signature(insn, sig).
LoadArrayIndex(base, to, inmethod) :-
  Instruction_Method(insn, inmethod),
  LoadArrayIndex_Base(insn, base),
  LoadArrayIndex_To(insn, to).
StoreArrayIndex(from, base, inmethod) :-
  Instruction_Method(insn, inmethod),
  StoreArrayIndex_From(insn, from),
  StoreArrayIndex_Base(insn, base).
AssignCast(type, from, to, inmethod) :-
  Instruction_Method(insn, inmethod),
  AssignCast_From(insn, from),
  AssignInstruction_To(insn, to),
  AssignCast_Type(insn, type).
AssignLocal(from, to, inmethod) :-
  AssignInstruction_To(insn, to),
  Instruction_Method(insn, inmethod),
  AssignLocal_From(insn, from).
AssignHeapAllocation(heap, to, inmethod) :-
  Instruction_Method(insn, inmethod),
  AssignHeapAllocation_Heap(insn, heap),
  AssignInstruction_To(insn, to).
ReturnVar(var, method) :-
  Instruction_Method(insn, method),
  ReturnNonvoid_Var(insn, var).
StaticMethodInvocation(invocation, signature, inmethod) :-
  isStaticMethodInvocation_Insn(invocation),
  Instruction_Method(invocation, inmethod),
  MethodInvocation_Method(invocation, signature).
VirtualMethodInvocation_SimpleName(invocation, simplename) :-
  isVirtualMethodInvocation_Insn(invocation),
  MethodInvocation_Method(invocation, signature),
  Method_SimpleName(signature, simplename),
  method_descriptor(signature, descriptor).
VirtualMethodInvocation_Descriptor(invocation, descriptor) :-
  isVirtualMethodInvocation_Insn(invocation),
  MethodInvocation_Method(invocation, signature),
  Method_SimpleName(signature, simplename),
  method_descriptor(signature, descriptor).

  
.decl MethodLookup(simplename:number, descriptor:number, type:number, method:number)
.decl MethodImplemented(simplename:number, descriptor:number, type:number, method:number)
.decl DirectSubclass(a:number, c:number)
.decl Subclass(c:number, a:number)
.decl Superclass(c:number, a:number)
.decl Superinterface(k:number, c:number)
.decl SubtypeOf(subtype:number, type:number)
.decl SupertypeOf(supertype:number, type:number)
.decl SubtypeOfDifferent(subtype:number, type:number)
.decl MainMethodDeclaration(method:number)

MethodLookup(simplename, descriptor, type, method) :-
  MethodImplemented(simplename, descriptor, type, method).
MethodLookup(simplename, descriptor, type, method) :-
  directsuperclass(type, supertype),
  MethodLookup(simplename, descriptor, supertype, method),
  ! MethodImplemented(simplename, descriptor, type, _).
MethodLookup(simplename, descriptor, type, method) :-
  directsuperinterface(type, supertype),
  MethodLookup(simplename, descriptor, supertype, method),
  ! MethodImplemented(simplename, descriptor, type, _).
MethodImplemented(simplename, descriptor, type, method) :-
  Method_SimpleName(method, simplename),
  method_descriptor(method, descriptor),
  Method_DeclaringType(method, type),
  ! method_modifier(2161502, method).
MainMethodDeclaration(method) :-
  mainclass(type),
  Method_DeclaringType(method, type),
  method != 590319,
  method != 1138805,
  method != 926489,
  Method_SimpleName(method, 2979023),
  method_descriptor(method, 3018506),
  method_modifier(976234, method),
  method_modifier(909718, method).

DirectSubclass(a, c) :-
  directsuperclass(a, c).
Subclass(c, a) :-
  DirectSubclass(a, c).
Subclass(c, a) :-
  Subclass(b, a),
  DirectSubclass(b, c).
Superclass(c, a) :-
  Subclass(a, c).
Superinterface(k, c) :-
  directsuperinterface(c, k).
Superinterface(k, c) :-
  directsuperinterface(c, j),
  Superinterface(k, j).
Superinterface(k, c) :-
  directsuperclass(c, super),
  Superinterface(k, super).

SubtypeOf(s, s) :-
  isClassType(s).
SubtypeOf(t, t) :-
  isType(t).
SubtypeOf(s, t) :-
  Subclass(t, s).
SubtypeOf(s, s) :-
  isInterfaceType(s).
SubtypeOf(s, t) :-
  isClassType(s),
  Superinterface(t, s).
SubtypeOf(s, t) :-
  isInterfaceType(s),
  isType(t),
  t = 427039.
SubtypeOf(s, t) :-
  isArrayType(s),
  isType(t),
  t = 427039.
SubtypeOf(s, t) :-
  isInterfaceType(s),
  Superinterface(t, s).
SubtypeOf(s, t) :-
  SubtypeOf(sc, tc),
  componenttype(s, sc),
  componenttype(t, tc),
  isReferenceType(sc),
  isReferenceType(tc).
SubtypeOf(s, t) :-
  isArrayType(s),
  isInterfaceType(t),
  isType(t),
  t = 1088934.
SubtypeOf(s, t) :-
  isArrayType(s),
  isInterfaceType(t),
  isType(t),
  t = 716731.

SupertypeOf(s, t) :-
  SubtypeOf(t, s).
SubtypeOfDifferent(s, t) :-
  SubtypeOf(s, t),
  s != t.

.decl ClassInitializer(type:number, method:number)
.decl InitializedClass(classOrInterface:number)

ClassInitializer(type, method) :-
   MethodImplemented(894110, 3016219, type, method).
InitializedClass(superclass) :-
   InitializedClass(class),
   directsuperclass(class, superclass).
InitializedClass(superinterface) :-
   InitializedClass(classOrInterface),
   directsuperinterface(classOrInterface, superinterface).
InitializedClass(class) :-
   MainMethodDeclaration(method),
   Method_DeclaringType(method, class).
InitializedClass(class) :-
   Reachable(inmethod),
   AssignHeapAllocation(heap, _, inmethod),
   heapallocation_type(heap, class).
InitializedClass(class) :-
   Reachable(inmethod),
   Instruction_Method(invocation, inmethod),
   isStaticMethodInvocation_Insn(invocation),
   MethodInvocation_Method(invocation, signature),
   Method_DeclaringType(signature, class).
InitializedClass(classOrInterface) :-
   Reachable(inmethod),
   StoreStaticField(_, signature, inmethod),
   Field_DeclaringType(signature, classOrInterface).
InitializedClass(classOrInterface) :-
   Reachable(inmethod),
   LoadStaticField(signature, _, inmethod),
   Field_DeclaringType(signature, classOrInterface).
Reachable(clinit) :-
   InitializedClass(class),
   ClassInitializer(class, clinit).

.decl Assign(to:number, from:number)
.decl VarPointsTo(heap:number, var:number)
.decl InstanceFieldPointsTo(heap:number , fld:number, baseheap:number)
.decl StaticFieldPointsTo(heap:number, fld:number)
.decl CallGraphEdge(invocation:number, meth:number)
.decl ArrayIndexPointsTo(baseheap:number, heap:number)
.decl Reachable(method:number)

Assign(actual, formal) :-
  CallGraphEdge(invocation, method),
  formalparam(index, method, formal),
  actualparam(index, invocation, actual).
Assign(return, local) :-
  CallGraphEdge(invocation, method),
  ReturnVar(return, method),
  assignreturnvalue(invocation, local).
VarPointsTo(heap, var) :-
  AssignHeapAllocation(heap, var, inMethod),
  Reachable(inMethod).
VarPointsTo(heap, to) :-
  Assign(from, to),
  VarPointsTo(heap, from).
VarPointsTo(heap, to) :-
  Reachable(inmethod),
  AssignLocal(from, to, inmethod),
  VarPointsTo(heap, from).
VarPointsTo(heap, to) :-
  Reachable(inmethod),
  AssignCast(type, from, to, inmethod),
  SupertypeOf(type, heaptype),
  heapallocation_type(heap, heaptype),
  VarPointsTo(heap, from).
ArrayIndexPointsTo(baseheap, heap) :-
  Reachable(inmethod),
  StoreArrayIndex(from, base, inmethod),
  VarPointsTo(baseheap, base),
  VarPointsTo(heap, from),
  heapallocation_type(heap, heaptype),
  heapallocation_type(baseheap, baseheaptype),
  componenttype(baseheaptype, componenttype),
  SupertypeOf(componenttype, heaptype).
VarPointsTo(heap, to) :-
  Reachable(inmethod),
  LoadArrayIndex(base, to, inmethod),
  VarPointsTo(baseheap, base),
  ArrayIndexPointsTo(baseheap, heap),
  var_type(to, type),
  heapallocation_type(baseheap, baseheaptype),
  componenttype(baseheaptype, basecomponenttype),
  SupertypeOf(type, basecomponenttype).
VarPointsTo(heap, to) :-
  Reachable(inmethod),
  LoadInstanceField(base, signature, to, inmethod),
  VarPointsTo(baseheap, base),
  InstanceFieldPointsTo(heap, signature, baseheap).
VarPointsTo(heap, to) :-
  Reachable(inmethod),
  LoadStaticField(fld, to, inmethod),
  StaticFieldPointsTo(heap, fld).
VarPointsTo(heap, this) :-
  Reachable(inMethod),
  Instruction_Method(invocation, inMethod),
  VirtualMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  heapallocation_type(heap, heaptype),
  VirtualMethodInvocation_SimpleName(invocation, simplename),
  VirtualMethodInvocation_Descriptor(invocation, descriptor),
  MethodLookup(simplename, descriptor, heaptype, toMethod),
  ThisVar(toMethod, this).
InstanceFieldPointsTo(heap, fld, baseheap) :-
  Reachable(inmethod),
  StoreInstanceField(from, base, fld, inmethod),
  VarPointsTo(heap, from),
  VarPointsTo(baseheap, base).
StaticFieldPointsTo(heap, fld) :-
  Reachable(inmethod),
  StoreStaticField(from, fld, inmethod),
  VarPointsTo(heap, from).

Reachable(toMethod) :-
  Reachable(inMethod),
  Instruction_Method(invocation, inMethod),
  VirtualMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  heapallocation_type(heap, heaptype),
  VirtualMethodInvocation_SimpleName(invocation, simplename),
  VirtualMethodInvocation_Descriptor(invocation, descriptor),
  MethodLookup(simplename, descriptor, heaptype, toMethod).
CallGraphEdge(invocation, toMethod) :-
  Reachable(inMethod),
  Instruction_Method(invocation, inMethod),
  VirtualMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  heapallocation_type(heap, heaptype),
  VirtualMethodInvocation_SimpleName(invocation, simplename),
  VirtualMethodInvocation_Descriptor(invocation, descriptor),
  MethodLookup(simplename, descriptor, heaptype, toMethod).

Reachable(tomethod) :-
  Reachable(inmethod),
  StaticMethodInvocation(invocation, tomethod, inmethod).
CallGraphEdge(invocation, tomethod) :-
  Reachable(inmethod),
  StaticMethodInvocation(invocation, tomethod, inmethod).

Reachable(tomethod) :-
  Reachable(inmethod),
  Instruction_Method(invocation, inmethod),
  SpecialMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  MethodInvocation_Method(invocation, tomethod),
  ThisVar(tomethod, this).
CallGraphEdge(invocation, tomethod) :-
  Reachable(inmethod),
  Instruction_Method(invocation, inmethod),
  SpecialMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  MethodInvocation_Method(invocation, tomethod),
  ThisVar(tomethod, this).
VarPointsTo(heap, this) :-
  Reachable(inmethod),
  Instruction_Method(invocation, inmethod),
  SpecialMethodInvocation_Base(invocation, base),
  VarPointsTo(heap, base),
  MethodInvocation_Method(invocation, tomethod),
  ThisVar(tomethod, this).

Reachable(method) :-
  MainMethodDeclaration(method).

.decl result(relation:symbol, tupcount:bigint)
result('MethodImplemented', count : { MethodImplemented(_, _, _, _) }).
result('DirectSubclass', count : { DirectSubclass(_, _) }).
result('Subclass', count : { Subclass(_, _) }).
result('Superclass', count : { Superclass(_, _) }).
result('Superinterface', count : { Superinterface(_, _) }).
result('SubtypeOf', count : { SubtypeOf(_, _) }).
result('SupertypeOf', count : { SupertypeOf(_, _) }).
result('SubtypeOfDifferent', count : { SubtypeOfDifferent(_, _) }).
result('MainMethodDeclaration', count : { MainMethodDeclaration(_) }).
result('ClassInitializer', count : { ClassInitializer(_, _) }).
result('InitializedClass', count : { InitializedClass(_) }).
result('Assign', count : { Assign(_, _) }).
result('VarPointsTo', count : { VarPointsTo(_, _) }).
result('InstanceFieldPointsTo', count : { InstanceFieldPointsTo(_, _, _) }).
result('StaticFieldPointsTo', count : { StaticFieldPointsTo(_, _) }).
result('CallGraphEdge', count : { CallGraphEdge(_, _) }).
result('ArrayIndexPointsTo', count : { ArrayIndexPointsTo(_, _) }).
result('Reachable', count : { Reachable(_) }).
result('MethodLookup', count : { MethodLookup(_, _, _, _) }).

  .output result
$$);
