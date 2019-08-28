//TODO write a description for this script
//@author 
//@category pib
//@keybinding 
//@menupath 
//@toolbar 

import java.util.ArrayList;
import java.util.Set;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.util.*;
import ghidra.program.model.reloc.*;
import ghidra.program.model.data.*;
import ghidra.program.model.block.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.scalar.*;
import ghidra.program.model.mem.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.listing.Function.FunctionUpdateType;
import ghidra.program.model.lang.*;
import ghidra.program.model.pcode.*;
import ghidra.program.model.address.*;

public class Session extends GhidraScript {
	PiBLib pib;
	boolean DRY_RUN = true;			// Does not apply changes to the ghidra database.
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_CYAN = "\u001B[36m";
	
	public void run() throws Exception {
		pib = new PiBLib(true, "none", this);
		FunctionManager fm = this.getCurrentProgram().getFunctionManager();
		String output = "\n";
		
		int client_date = pib.checkDate();
		if (client_date == -1) {
			printerr("Client date was not found!");
			return;
		}
		
		/* header and static things */
		output += "//\n";
		output += "// CSession defines\n";
		output += "//\n";
		output += "typedef signed int(__thiscall* lpGetTalkType)(void*, char*, int, char*);\n";
		output += "typedef void(__thiscall* lpRecalcAveragePingTime)(void*, unsigned long);\n";
		output += "\n";
		output += "//struct CSession {\n";
		output += "#define SESSION_DATA   \\\n";
		output += "\tTODO\n";
		output += "//};\n\n";
		
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for CSession__GetTalkType
    	//
		ArrayList<Function> ref_list = (ArrayList<Function>) pib.string_to_reflist(LISTTYPE.ARRAYLIST, "/deal");
		if (ref_list.size() == 0) {
			printerr("ref_list for /deal was empty!");
			return;
		}
		
		if (ref_list.size() > 1) {
			printerr("more then one reference for /deal was found!");
			return;
		}
		
		var func_entry = ref_list.get(0).getEntryPoint();
		pib.log("fun_entry: " + func_entry.toString());
		output += "#define GETTALKTYPE_FN 0x" + func_entry + "\n";
		
		/* set function options */
		var CSession_GetTalkType_fn = fm.getFunctionContaining(func_entry);
		CSession_GetTalkType_fn.setName("CSession::GetTalkType", SourceType.USER_DEFINED);
		
		if (!DRY_RUN) {
			if (client_date >= 20150513) {
				printerr("TODO");
			} else {
				printerr("Unable to set function parameters for CSession::GetTalkType: Clientdate: " + client_date);
				return;
			}
		}
		
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for CSession__RecalcAveragePingTime
    	//
		ArrayList<Function> ref_set_pinglog = (ArrayList<Function>) pib.string_to_reflist(LISTTYPE.ARRAYLIST, "PingLog.txt");
		ArrayList<Function> ref_set_format = (ArrayList<Function>) pib.string_to_reflist(LISTTYPE.ARRAYLIST, "%10.2f\t%10d\n");
		
		if (ref_set_pinglog.size() < 1) {
			printerr("ref_set_pinglog is < 1");
			return;
		}
		
		if (ref_set_format.size() != 1) {
			printerr("ref_set_format size is unequal 1");
			return;
		}
		
		/* check if PingLog.txt and format are in the same function */
		var tmp_func_fmt = ref_set_format.get(0);
		Function recalc_pre_func = null;
		for (var ref : ref_set_pinglog) {
			if (tmp_func_fmt == ref) {
				recalc_pre_func = ref;
				break;
			}
		}
		
		if (recalc_pre_func == null) {
			printerr("recalc_pre_func was null!");
			return;
		}
		
		pib.log("recalc_pre_func: " + recalc_pre_func);
		String sig = pib.toSignature("b9.{4} 5d.{5}");
		Address[] sig_addr_lst = findBytes(recalc_pre_func.getBody(), sig, 1, 1, false);
		if (sig_addr_lst == null) {
			printerr("Failed to get signature inside recalc_pre_func.getBody()");
			return;
		}
		
		if (sig_addr_lst.length == 0) {
			printerr("Signature 'b9.{4} 5d.{5}' was not found!");
			return;
		}
		
		if (sig_addr_lst.length > 1) {
			printerr("Found multiple matching signatures!");
			return;
		}
		
		var recalc_fun_instr = getInstructionAt(sig_addr_lst[0].add(6 /* size of signature - jmp instr*/));
		var recalc_fn = parseAddress(recalc_fun_instr.toString().replace("JMP ", "")); /* strip the jmp opcode string */
		pib.log("Address after signature: " + recalc_fn);
		
		output += "#define RECALCAVERAGEPINGTIME_FN 0x" + recalc_fn + "\n";
		
		/* set function options */
		var CSession_RecalcAveragePingTime_fn = fm.getFunctionContaining(recalc_fn);
		CSession_RecalcAveragePingTime_fn.setName("CSession::RecalcAveragePingTime", SourceType.USER_DEFINED);
		
		if (!DRY_RUN) {
			if (client_date >= 20150513) {
				printerr("TODO");
			} else {
				printerr("Unable to set function parameters for CSession::RecalcAveragePingTime: Clientdate: " + client_date);
				return;
			}
		}
		
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for Session pointer
    	//
		ReferenceIterator iter = getCurrentProgram().getReferenceManager().getReferencesTo(CSession_GetTalkType_fn.getEntryPoint());;
		/* verify that all MOV EXC, global_offset instructions use the same global offset for session ptr */
		Address session_ptr = null;
		for (var fn : iter) {
			var caller_addr = fn.getFromAddress();
			//pib.log("CSession_GetTalkType ref: " + caller_addr);
			var tmp_addr = parseAddress(getInstructionAt(caller_addr.subtract(5 /* size of mov instr */)).toString().replace("MOV ECX,", ""));
			//pib.log("CSession_GetTalkType ptr: " + tmp_addr);
			if (session_ptr == null) {
				session_ptr = tmp_addr;
			} else {
				if (session_ptr.subtract(tmp_addr) != 0) {
					printerr("not all session pointers match! " + session_ptr + " ?= " + tmp_addr);
					return;
				}
			}
		}
		pib.log("Session Ptr: " + session_ptr);
		output += "#define SESSION_PTR 0x" + session_ptr + "\n";
		print(output);
    }

}
