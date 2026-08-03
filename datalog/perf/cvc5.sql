CREATE TABLE Arch_memory_access(AccessType INTEGER NOT NULL,EA INTEGER NOT NULL,DirectReg INTEGER NOT NULL,BaseReg INTEGER NOT NULL,IndexReg INTEGER NOT NULL,Offset_ INTEGER NOT NULL);
COPY Arch_memory_access FROM 'Arch_memory_access_truncate.csv' WITH (FORMAT 'csv', DELIMITER ",");

CREATE TABLE Arch_reg_reg_arithmetic_operation(EA INTEGER NOT NULL,Dst INTEGER NOT NULL,Src1 INTEGER NOT NULL,Src2 INTEGER NOT NULL,Mult INTEGER NOT NULL,Offset_ INTEGER NOT NULL);
COPY Arch_reg_reg_arithmetic_operation FROM 'Arch_reg_reg_arithmetic_operation.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Arch_return_reg(Reg INTEGER NOT NULL);
COPY Arch_return_reg FROM 'Arch_return_reg.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Block_next(Block INTEGER NOT NULL,BlockEnd INTEGER NOT NULL,NextBlock INTEGER NOT NULL);
COPY Block_next FROM 'Block_next.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Block_last_instruction(Block INTEGER NOT NULL,EA INTEGER NOT NULL);
COPY Block_last_instruction FROM 'Block_last_instruction.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Code_in_block(EA INTEGER NOT NULL,Block INTEGER NOT NULL);
COPY Code_in_block FROM 'Code_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Direct_call(EA INTEGER NOT NULL,Dest INTEGER NOT NULL);
COPY Direct_call FROM 'Direct_call.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE May_fallthrough(o INTEGER NOT NULL,d INTEGER NOT NULL);
COPY May_fallthrough FROM 'May_fallthrough.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_block_last_def(EA INTEGER NOT NULL,EA_def INTEGER NOT NULL,Var INTEGER NOT NULL);
COPY Reg_def_use_block_last_def FROM 'Reg_def_use_block_last_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_defined_in_block(Block INTEGER NOT NULL,Var INTEGER NOT NULL);
COPY Reg_def_use_defined_in_block FROM 'Reg_def_use_defined_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_flow_def(EA INTEGER NOT NULL,Var INTEGER NOT NULL,EA_next INTEGER NOT NULL,Value INTEGER NOT NULL);
COPY Reg_def_use_flow_def FROM 'Reg_def_use_flow_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_live_var_def(Block INTEGER NOT NULL,VarIdentity INTEGER NOT NULL,LiveVar INTEGER NOT NULL,EA_def INTEGER NOT NULL);
COPY Reg_def_use_live_var_def FROM 'Reg_def_use_live_var_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_ref_in_block(Block INTEGER NOT NULL,Var INTEGER NOT NULL);
COPY Reg_def_use_ref_in_block FROM 'Reg_def_use_ref_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_return_block_end(Callee INTEGER NOT NULL,CalleeEnd INTEGER NOT NULL,Block INTEGER NOT NULL,BlockEnd INTEGER NOT NULL);
COPY Reg_def_use_return_block_end FROM 'Reg_def_use_return_block_end.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_used(EA INTEGER NOT NULL,Var INTEGER NOT NULL,Index INTEGER NOT NULL);
COPY Reg_def_use_used FROM 'Reg_def_use_used.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_def_use_used_in_block(Block INTEGER NOT NULL,EA_used INTEGER NOT NULL,Var INTEGER NOT NULL,Index INTEGER NOT NULL);
COPY Reg_def_use_used_in_block FROM 'Reg_def_use_used_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Reg_used_for(EA INTEGER NOT NULL,Reg INTEGER NOT NULL,Type INTEGER NOT NULL);
COPY Reg_used_for FROM 'Reg_used_for.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Relative_jump_table_entry_candidate(EA INTEGER NOT NULL,TableStart INTEGER NOT NULL,Size INTEGER NOT NULL,Reference INTEGER NOT NULL,Dest INTEGER NOT NULL,Scale INTEGER NOT NULL,Offset_ INTEGER NOT NULL);
COPY Relative_jump_table_entry_candidate FROM 'Relative_jump_table_entry_candidate.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_def(EA INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL);
COPY Stack_def_use_def FROM 'Stack_def_use_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_defined_in_block(Block INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL);
COPY Stack_def_use_defined_in_block FROM 'Stack_def_use_defined_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_live_var_def(Block INTEGER NOT NULL,VarIdentityReg INTEGER NOT NULL,VarIdentityPos INTEGER NOT NULL,LiveVarReg INTEGER NOT NULL,LiveVarPos INTEGER NOT NULL,EA_def INTEGER NOT NULL);
COPY Stack_def_use_live_var_def FROM 'Stack_def_use_live_var_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_ref_in_block(Block INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL);
COPY Stack_def_use_ref_in_block FROM 'Stack_def_use_ref_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_used_in_block(Block INTEGER NOT NULL,EA_used INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL,Index INTEGER NOT NULL);
COPY Stack_def_use_used_in_block FROM 'Stack_def_use_used_in_block.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_used(EA INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL,Index INTEGER NOT NULL);
COPY Stack_def_use_used FROM 'Stack_def_use_used.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_live_var_used(Block INTEGER NOT NULL,LiveVarReg INTEGER NOT NULL,LiveVarPos INTEGER NOT NULL,UsedVarReg INTEGER NOT NULL,UsedVarPos INTEGER NOT NULL,EA_used INTEGER NOT NULL,Index INTEGER NOT NULL,Moves INTEGER NOT NULL);
COPY Stack_def_use_live_var_used FROM 'Stack_def_use_live_var_used.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Jump_table_start(EA_jump INTEGER NOT NULL,Size INTEGER NOT NULL,TableStart INTEGER NOT NULL,TableRef INTEGER NOT NULL,Scale INTEGER NOT NULL);
COPY Jump_table_start FROM 'Jump_table_start.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE def_used_for_address_0(EA_def INTEGER NOT NULL,Reg INTEGER NOT NULL,Type INTEGER NOT NULL);
COPY def_used_for_address_0 FROM 'Def_used_for_address_0.csv' WITH (FORMAT 'csv', DELIMITER ',');

CREATE TABLE Stack_def_use_block_last_def(EA INTEGER NOT NULL,EA_def INTEGER NOT NULL,VarReg INTEGER NOT NULL,VarPos INTEGER NOT NULL);
COPY Stack_def_use_block_last_def FROM 'Stack_def_use_block_last_def.csv' WITH (FORMAT 'csv', DELIMITER ',');

SELECT * FROM umbra.datalog($$
.use arch_memory_access
.use arch_reg_reg_arithmetic_operation
.use arch_return_reg
.use block_next
.use block_last_instruction
.use code_in_block
.use direct_call
.use may_fallthrough
.use reg_def_use_block_last_def
.use reg_def_use_defined_in_block
.use reg_def_use_flow_def
.use reg_def_use_live_var_def
.use reg_def_use_ref_in_block
.use reg_def_use_return_block_end
.use reg_def_use_used
.use reg_def_use_used_in_block
.use reg_used_for
.use relative_jump_table_entry_candidate
.use stack_def_use_def
.use stack_def_use_defined_in_block
.use stack_def_use_live_var_def
.use stack_def_use_ref_in_block
.use stack_def_use_used_in_block
.use stack_def_use_used
.use stack_def_use_live_var_used
.use jump_table_start
.use def_used_for_address_0
.use stack_def_use_block_last_def
.decl Stack_def_use_def_used(EA_def:number,VarDefReg:number,VarDefPos:number,EA_used:number,VarUsedReg:number,VarUsedPos:number)

.decl Reg_def_use_return_val_used(EA_call:number,Callee:number,Reg:number,EA_used:number,Index_used:number)

.decl Reg_def_use_live_var_used(Block:number,LiveVar:number,EA_used:number,Index:number) 

.decl Jump_table_target(EA:number,Dest:number)

.decl Reg_def_use_def_used(EA_def:number,Var:number,EA_used:number,Index_used:number)

.decl Reg_def_use_live_var_at_prior_used(EA_used:number,BlockUsed:number,Var:number)

.decl Reg_def_use_live_var_at_block_end(Block:number,BlockUsed:number,Var:number)

.decl Reg_reg_arithmetic_operation_defs(EA:number,Reg_def:number,EA_def1:number,Reg1:number,EA_def2:number,Reg2:number,Mult:number,Offset_:number)

.decl Def_used_for_address(EA_def:number,Reg:number,Type:number)

.decl Stack_def_use_live_var_at_block_end(Block:number,BlockUsed:number,VarReg:number,VarPos:number)

.decl Stack_def_use_live_var_at_prior_used(EA_used:number,BlockUsed:number,VarReg:number,VarPos:number)




Jump_table_target(EA,Dest) :- 
   jump_table_start(EA,Size,TableStart,_,_), 
   relative_jump_table_entry_candidate(_,TableStart,Size,_,Dest,_,_).


Reg_def_use_def_used(EA_def,Var,EA_used,Index) :- 
   reg_def_use_used(EA_used,Var,Index),
   reg_def_use_block_last_def(EA_used,EA_def,Var).


Reg_def_use_def_used(EA_def,VarIdentity,EA_used,Index) :- 
   Reg_def_use_live_var_at_block_end(Block,BlockUsed,Var),
   reg_def_use_live_var_def(Block,VarIdentity,Var,EA_def),
   Reg_def_use_live_var_used(BlockUsed,Var,EA_used,Index).  

Reg_def_use_def_used(EA_def,Var,Next_EA_used,NextIndex) :- 
   Reg_def_use_live_var_at_prior_used(EA_used,NextUsedBlock,Var),
   Reg_def_use_def_used(EA_def,Var,EA_used,_),
   Reg_def_use_live_var_used(NextUsedBlock,Var,Next_EA_used,NextIndex).  

Reg_def_use_def_used(EA_def,Reg,EA_used,Index) :- 
   Reg_def_use_return_val_used(_,Callee,Reg,EA_used,Index),
   reg_def_use_return_block_end(Callee,_,_,BlockEnd),
   reg_def_use_block_last_def(BlockEnd,EA_def,Reg).


Reg_def_use_return_val_used(EA_call,Callee,Reg,EA_used,Index_used) :- 
   arch_return_reg(Reg),
   Reg_def_use_def_used(EA_call,Reg,EA_used,Index_used),
   direct_call(EA_call,Callee).


Reg_def_use_live_var_used(Block,Var,EA_used,Index) :- 
   reg_def_use_used_in_block(Block,EA_used,Var,Index),
   !reg_def_use_block_last_def(EA_used,_,Var).

Reg_def_use_live_var_used(RetBlock,Reg,EA_used,Index) :- 
   reg_def_use_return_block_end(Callee,_,RetBlock,RetBlockEnd),
   !reg_def_use_block_last_def(RetBlockEnd,_,Reg),
   Reg_def_use_return_val_used(_,Callee,Reg,EA_used,Index).


Reg_def_use_live_var_at_prior_used(EA_used,BlockUsed,Var) :- 
   Reg_def_use_live_var_at_block_end(Block,BlockUsed,Var),
   reg_def_use_used_in_block(Block,EA_used,Var,_),
   !reg_def_use_defined_in_block(Block,Var).


Reg_def_use_live_var_at_block_end(PrevBlock,Block,Var) :- 
   block_next(PrevBlock,PrevBlockEnd,Block),
   Reg_def_use_live_var_used(Block,Var,_,_),
   !reg_def_use_flow_def(PrevBlockEnd,Var,Block,_).

Reg_def_use_live_var_at_block_end(PrevBlock,BlockUsed,Var) :- 
   Reg_def_use_live_var_at_block_end(Block,BlockUsed,Var),
   !reg_def_use_ref_in_block(Block,Var),
   block_next(PrevBlock,_,Block). 


Reg_reg_arithmetic_operation_defs(EA,Reg_def,EA_def1,Reg1,EA_def2,Reg2,Mult,Offset) :- 
   Def_used_for_address(EA,Reg_def,_),
   arch_reg_reg_arithmetic_operation(EA,Reg_def,Reg1,Reg2,Mult,Offset),
   Reg1 != Reg2,
   Reg_def_use_def_used(EA_def1,Reg1,EA,_),
   EA != EA_def1,
   Reg_def_use_def_used(EA_def2,Reg2,EA,_),
   EA != EA_def2.

Def_used_for_address(EA,Reg,Type) :- 
   def_used_for_address_0(EA,Reg,Type),
   Type = 371929.

Def_used_for_address(EA_def,Reg,Type) :- 
   Reg_def_use_def_used(EA_def,Reg,EA,_),
   reg_used_for(EA,Reg,Type).

Def_used_for_address(EA_def,Reg,Type) :- 
   Def_used_for_address(EA_used,_,Type),
   Reg_def_use_def_used(EA_def,Reg,EA_used,_).

Def_used_for_address(EA_def,Reg1,Type) :- 
   Def_used_for_address(EALoad,Reg2,Type),
   arch_memory_access(332573,EALoad,Reg2,RegBaseLoad,332575,StackPosLoad),
   Stack_def_use_def_used(EAStore,RegBaseStore,StackPosStore,EALoad,RegBaseLoad,StackPosLoad), 
   arch_memory_access(333071,EAStore,Reg1,RegBaseStore,332575,StackPosStore),
   Reg_def_use_def_used(EA_def,Reg1,EAStore,_).

Stack_def_use_def_used(EA_def,Varr,Varp,EA_used,Varr,Varp) :- 
   stack_def_use_used(EA_used,Varr,Varp,_),
   stack_def_use_block_last_def(EA_used,EA_def,Varr,Varp).

Stack_def_use_def_used(EA_def,DefVarr,DefVarp,EA_used,VarUsedr,VarUsedp) :- 
   Stack_def_use_live_var_at_block_end(Block,BlockUsed,Varr,Varp),
   stack_def_use_live_var_def(Block,DefVarr,DefVarp,Varr,Varp,EA_def),
   stack_def_use_live_var_used(BlockUsed,Varr,Varp,VarUsedr,VarUsedp,EA_used,_,_). 

Stack_def_use_def_used(EA_def,DefVarr,DefVarp,EA_used,UsedVarr,UsedVarp) :- 
   stack_def_use_live_var_used(EA,DefVarr,DefVarp,UsedVarr,UsedVarp,EA_used,_,_), 
   may_fallthrough(EA_def,EA),
   code_in_block(EA_def,Block),
   code_in_block(EA,Block),
   stack_def_use_def(EA_def,DefVarr,DefVarp).
 
Stack_def_use_def_used(EA_def,VarDefr,VarDefp,Next_EA_used,VarUsedr,VarUsedp) :- 
   Stack_def_use_live_var_at_prior_used(EA_used,NextUsedBlock,Varr,Varp),
   Stack_def_use_def_used(EA_def,VarDefr,VarDefp,EA_used,Varr,Varp),
   stack_def_use_live_var_used(NextUsedBlock,Varr,Varp,VarUsedr,VarUsedp,Next_EA_used,_,_). 

Stack_def_use_live_var_at_block_end(PrevBlock,BlockUsed,inlined_BaseReg_374,inlined_StackPos_374) :- 
   Stack_def_use_live_var_at_block_end(Block,BlockUsed,inlined_BaseReg_374,inlined_StackPos_374),
   !stack_def_use_ref_in_block(Block,inlined_BaseReg_374,inlined_StackPos_374),
   !reg_def_use_defined_in_block(Block,inlined_BaseReg_374),
   block_next(PrevBlock,_,Block).

Stack_def_use_live_var_at_block_end(PrevBlock,Block,Varr,Varp) :- 
   block_next(PrevBlock,_,Block),
   stack_def_use_live_var_used(Block,Varr,Varp,_,_,_,_,_). 

Stack_def_use_live_var_at_prior_used(EA_used,BlockUsed,inlined_BaseReg_375,inlined_StackPos_375) :- 
   Stack_def_use_live_var_at_block_end(Block,BlockUsed,inlined_BaseReg_375,inlined_StackPos_375),
   stack_def_use_used_in_block(Block,EA_used,inlined_BaseReg_375,inlined_StackPos_375,_),
   !reg_def_use_defined_in_block(Block,inlined_BaseReg_375),
   !stack_def_use_defined_in_block(Block,inlined_BaseReg_375,inlined_StackPos_375).

.decl result(relation:symbol, tupcount:bigint)

result('Stack_def_use_def_used', count : { Stack_def_use_def_used(_, _, _, _, _, _) }).
result('Reg_def_use_return_val_used', count : { Reg_def_use_return_val_used(_, _, _, _, _) }).
result('Reg_def_use_live_var_used', count : { Reg_def_use_live_var_used(_, _, _, _) }).
result('Jump_table_target', count : { Jump_table_target(_, _) }).
result('Reg_def_use_def_used', count : { Reg_def_use_def_used(_, _, _, _) }).
result('Reg_def_use_live_var_at_prior_used', count : { Reg_def_use_live_var_at_prior_used(_, _, _) }).
result('Reg_def_use_live_var_at_block_end', count : { Reg_def_use_live_var_at_block_end(_, _, _) }).
result('Reg_reg_arithmetic_operation_defs', count : { Reg_reg_arithmetic_operation_defs(_, _, _, _, _, _, _, _) }).
result('Def_used_for_address', count : { Def_used_for_address(_, _, _) }).
result('Stack_def_use_live_var_at_block_end', count : { Stack_def_use_live_var_at_block_end(_, _, _, _) }).
result('Stack_def_use_live_var_at_prior_used', count : { Stack_def_use_live_var_at_prior_used(_, _, _, _) }).
.output result

$$);
