//TODO write a description for this script
//@author 
//@category pib
//@keybinding 
//@menupath 
//@toolbar 

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.nio.charset.*;

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

public class UIWindowMgr extends GhidraScript {
	final int INSTR_COUNT = 9;
	final boolean DEBUG = true;
	PiBLib pib;

	public String toSignature(String signature) {
		return "\\x" + signature.replace(" ", "\\x");
	}
	
	public void printdbg(String str) {
		if (DEBUG)
			println(str);
	}

    public void run() throws Exception {
    	pib = new PiBLib(false, "none", this);
		FunctionManager fm = this.getCurrentProgram().getFunctionManager();
		
		/* Check date */
		String ragexe = toSignature("44 3a 5c 53 6f 75 72 63 65 5c 4b 6f 72 65 61 5c 52 4f 5f");
		printdbg("Looking for: " + ragexe);
		
		Address ragexe_addr = findBytes(null, ragexe);
		if(ragexe_addr == null) {
			printerr("Unable to find client date!");
			return;
		}
		
		printdbg("Client date: " + ragexe_addr);
		byte[] bytes = new byte[256];
		int c = 0;
		while(true) {
			if (getByte(ragexe_addr) == 0)
				break;
			
			if (c > 255)
				break;
			
			bytes[c] = getByte(ragexe_addr);
			ragexe_addr = ragexe_addr.next();
			c++;
		}
		String byte_str = new String(bytes, StandardCharsets.UTF_8);
		printdbg("Client date string: " + byte_str);
		
		String client_date_str = null;
		var split_str = byte_str.split("\\\\");
		for (var substr : split_str) {
			printdbg("Substr: " + substr);
			if (substr.startsWith("RO_")) {
				client_date_str = substr.replace("RO_", "").replace("-", "");
				printdbg("Client date number: " + client_date_str);
				break;
			}	
		}
		
		if (client_date_str == null) {
			printerr("Failed to get Client date!");
			return;
		}
		
		int client_date = Integer.parseInt(client_date_str);
    	
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for UIWindowMgr__MakeWindow
    	//
		String sig = toSignature("e8.{0,4} 8b c8 e8.{0,4} 50 e8.{0,4} 8b c8 e8.{0,4} 6a 06 b9.{0,4} e8.{0,4} 6a 01 6a 10 6a 7a 8b f0 e8.{0,4}");
		printdbg("Looking for: " + sig);
		
		Address sig_addr = findBytes(null, sig);
		if (sig_addr == null) {
			printerr("UIWindowMgr_MakeWindow: signatur not found!");
			return;
		}
		
		printdbg("Signature found at: " + sig_addr.toString());
		
		var instr = getInstructionAt(sig_addr);
		for (int i = 0; i < INSTR_COUNT; i++)
			instr = instr.getNext();
		
		printdbg("UIWindowMgr__MakeWindow: " + instr.toString());
		
		/* header */
		print("//\n");
		print("// CUIWindowMgr defines\n");
		print("//\n");
		
		/* parsed address for UIWindowMgr__MakeWindow */
		Address addr = parseAddress(instr.toString().replace("CALL ", ""));
		print("#define UIWINDOWMGR_MAKEWINDOW_FN 0x" + addr + "\n");
		
		/* set function parameters */
		if (client_date >= 20150513) {
			printdbg("Config function UIWINDOWMGR_MAKEWINDOW_FN: " + addr);
			var UIWindowMgr_MakeWindow_fn = fm.getFunctionContaining(addr);
			UIWindowMgr_MakeWindow_fn.setName("UIWindowMgr::MakeWindow", SourceType.USER_DEFINED);
			
			var this_ptr = 	new ParameterImpl("this", new Pointer32DataType(), this.getCurrentProgram());
			var a1 = 		new ParameterImpl("a1", new IntegerDataType(), this.getCurrentProgram());
			var ret_var = 	new ReturnParameterImpl(new Pointer32DataType(), this.getCurrentProgram());
			
			UIWindowMgr_MakeWindow_fn.updateFunction("__thiscall", ret_var, 
					FunctionUpdateType.DYNAMIC_STORAGE_ALL_PARAMS, false, 
					SourceType.USER_DEFINED, this_ptr, a1);
		} else {
			printerr("Unable to set function parameters for UIWindowMgr::MakeWindow: Clientdate: " + client_date);
			return;
		}
		
		/* add static content */
		print("\n");
		print("struct UIFrameWnd {};\n");
		print("typedef UIFrameWnd* (__thiscall* lpMakeWindow)(void*, int);\n");
		print("\n");
		
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for UIBookWnd__SendMsg
    	//
		
		/* add static content */
		print("\t// sub types\n");
		print("\t// CUIBookWnd\n");
		
		
		/* search function */
		Set<Function> func_set = pib.string_to_reflist("%sbook\\%s.txt");
		
		Function func_addr = null;
		int counter = 0;
		sig = toSignature("6a 00 6a 00 6a 00 68.{0,4} 6a 06");
		for(Iterator<Function> it = func_set.iterator(); it.hasNext();) {
			func_addr = it.next();
			if (func_addr == null) {
				printdbg("func_addr was null!");
				continue;
			}
			
			var sig_addr_lst = findBytes(func_addr.getBody(), sig, 1, 2, false);
			if (sig_addr_lst.length > 0) {
				printdbg("Signature found at: " + sig_addr_lst[0]);
				counter++;
			}
		}
		
		if (counter > 1) {
			printerr("UIWindowMgr: counter was greater 1");
			return;
		}
			
		printdbg("UIBookWnd__SendMsg: " + func_addr.getEntryPoint());
		print("\t#define UIBOOKWND_SENDMSG_FN 0x" + func_addr.getEntryPoint() + "\n");
		
		/* set function parameters */
		if (client_date >= 20150513) {
			var UIBookWnd_SendMsg_fn = fm.getFunctionContaining(func_addr.getEntryPoint());
			UIBookWnd_SendMsg_fn.setName("UIBookWnd::SendMsg", SourceType.USER_DEFINED);
			
			var this_ptr = 	new ParameterImpl("this", new Pointer32DataType(), this.getCurrentProgram());
			var a1 = 		new ParameterImpl("a1", new IntegerDataType(), this.getCurrentProgram());
			var a2 =		new ParameterImpl("a2", new IntegerDataType(), this.getCurrentProgram());
			var a3 =		new ParameterImpl("a3", new Pointer8DataType(), this.getCurrentProgram());
			var a4 = 		new ParameterImpl("a4", new IntegerDataType(), this.getCurrentProgram());
			var a5 =		new ParameterImpl("a5", new IntegerDataType(), this.getCurrentProgram());
			var a6 =		new ParameterImpl("a6", new IntegerDataType(), this.getCurrentProgram());
			var ret_var = 	new ReturnParameterImpl(new VoidDataType(), this.getCurrentProgram());
			
			UIBookWnd_SendMsg_fn.updateFunction("__thiscall", ret_var, 
					FunctionUpdateType.DYNAMIC_STORAGE_ALL_PARAMS, false, 
					SourceType.USER_DEFINED, 
					this_ptr, a1, a2, a3, a4, a5, a6);
		} else {
			printerr("Unable to set function parameters for UIWindowMgr::MakeWindow: Clientdate: " + client_date);
			return;
		}
		
		/* add static content */
		print("\n");
		print("\ttypedef void(__thiscall* lpSendMsg)(void*, int, int, char*, int, int, int);\n");
		print("\t//struct CUIBookWnd {\n");
		print("\t#define UIBOOKWND_DATA		\\\n");
		
		//
		// Searching for book_title location
		//
		print("\t\tTODO\n");
		print("\t//}\n");
		
   	}
}
