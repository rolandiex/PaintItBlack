//TODO write a description for this script
//@author 
//@category pib
//@keybinding 
//@menupath 
//@toolbar 

import java.util.Iterator;
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
import ghidra.program.model.lang.*;
import ghidra.program.model.pcode.*;
import ghidra.program.model.address.*;

public class UIWindowMgr extends GhidraScript {
	final int INSTR_COUNT = 9;
	final boolean DEBUG = false;
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
    	
    	//
    	// Searching for UIWindowMgr__MakeWindow
    	//
		String sig = toSignature("e8.{0,4} 8b c8 e8.{0,4} 50 e8.{0,4} 8b c8 e8.{0,4} 6a 06 b9.{0,4} e8.{0,4} 6a 01 6a 10 6a 7a 8b f0 e8.{0,4}");
		printdbg(sig);
		
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
		print("#define UIWINDOWMGR_MAKEWINDOW_FN " + instr.toString().replace("CALL ", "") + "\n");
		
		/* add static content */
		print("\n");
		print("struct UIFrameWnd {};\n");
		print("typedef UIFrameWnd* (__thiscall* lpMakeWindow)(void*, int);\n");
		print("\n");
		print("\t// sub types\n");
		print("\t// CUIBookWnd\n");
		
		//
		// Searching for UIBookWnd__SendMsg
		//
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
		
		print("\t#define UIBOOKWND_SENDMSG_FN " + func_addr.toString().replace("FUN_", "0x") + "\n");
		
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
