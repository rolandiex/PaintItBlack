//This script searches UIWindowMgr related functions and addresses.
//- UIBookWnd__SendMsg
//- UIWindowMgr__MakeWindow
//It also generates the <client_date>.h file for Paint it Black
//@author Norman Ziebal
//@category pib
//@keybinding 
//@menupath 
//@toolbar 

import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

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
	boolean DRY_RUN = true;			// Does not apply changes to the ghidra database.
	PiBLib pib;
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_CYAN = "\u001B[36m";
	
	public void run() throws Exception {
		pib = new PiBLib(true, "none", this);
		String[] args = getScriptArgs();
		boolean TEST_RUN = false;
		boolean HEADLESS = false;
		
		/* parsing command line args */
		for (var arg : args) {
			if (arg.equals("headless"))
				HEADLESS = true;

			if (arg.equals("dry-run"))
				DRY_RUN = true;

			if (arg.equals("test"))
				TEST_RUN = true;
		}	
		
		/* running headless */
		if (HEADLESS) {
			if (TEST_RUN) {
				run_test();
				return;
			}
			print(ANSI_CYAN + execute() + ANSI_RESET);
			return;
		}

		/* running tests with gui */
	    int dialogButton = JOptionPane.YES_NO_OPTION;
	    int dialogResult = JOptionPane.showConfirmDialog (null, "Run tests?","Warning",dialogButton);
	    if(dialogResult == JOptionPane.YES_OPTION){
			run_test();
	    	return;
	    }
	    
	    /* running normal mode with gui */
		dialogResult = JOptionPane.showConfirmDialog (null, "Dry run?","Warning",dialogButton);
		if(dialogResult == JOptionPane.NO_OPTION){
		  DRY_RUN = false;
		}
		print(execute());
	}

    public String execute() throws Exception {
		FunctionManager fm = this.getCurrentProgram().getFunctionManager();
		
		String output = "\n";
		
		int client_date = pib.checkDate();
		if (client_date == -1) {
			printerr("Client date was not found!");
			return "";
		}
    	
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for UIWindowMgr__MakeWindow
    	//
		String sig = pib.toSignature("e8.{0,4} 8b c8 e8.{0,4} 50 e8.{0,4} 8b c8 e8.{0,4} 6a 06 b9.{0,4} e8.{0,4} 6a 01 6a 10 6a 7a 8b f0 e8.{0,4}");
		pib.log("Looking for: " + sig);
		
		Address sig_addr = findBytes(null, sig);
		if (sig_addr == null) {
			printerr("UIWindowMgr_MakeWindow: signatur not found!");
			return "";
		}
		
		pib.log("Signature found at: " + sig_addr.toString());
		
		var instr = getInstructionAt(sig_addr);
		for (int i = 0; i < INSTR_COUNT; i++) // Its the 9. instruction after the signature address
			instr = instr.getNext();
		
		pib.log("UIWindowMgr__MakeWindow: " + instr.toString());
		
		/* header */
		output += "//\n";
		output += "// CUIWindowMgr defines\n";
		output += "//\n";
		
		/* parsed address for UIWindowMgr__MakeWindow */
		Address addr = parseAddress(instr.toString().replace("CALL ", ""));
		output += "#define UIWINDOWMGR_MAKEWINDOW_FN 0x" + addr + "\n";
		
		/* get UIWindowMgr_PTR */
		pib.log("Searching UIWindowMgr_PTR. Start search at: " + instr.getAddress().toString());
		Address tmp = null;
		for (int i = 0; i < 5; /* only check the previous 5 instructions */ i++) {
			 tmp = instr.getPrevious().getAddress(); // instr: CALL UIWindowMgr_MakeWindow
			 if (getByte(tmp) == 0xb9) { // looking for mov ecx
				 pib.log("UIWindowMgr_PTR: MOV ECX found!");
				 break;
			 }
		}
		
		var mov_ecx = getInstructionAt(tmp); // this should be MOV ECX, UIWINDOWMGR_PTR now!
		pib.log("UIWindowMgr_PTR: " + mov_ecx);
		var UIWindowMgr_PTR = parseAddress(mov_ecx.toString().replace("MOV ECX,", ""));
		pib.log("UIWindowMgr_PTR address: " + UIWindowMgr_PTR);
		output += "#define UIWINDOWMGR_PTR 0x" + UIWindowMgr_PTR.toString();
		
		/* set function parameters */
		pib.log("Config function UIWINDOWMGR_MAKEWINDOW_FN: " + addr);
		var UIWindowMgr_MakeWindow_fn = fm.getFunctionContaining(addr);
		UIWindowMgr_MakeWindow_fn.setName("UIWindowMgr::MakeWindow", SourceType.USER_DEFINED);
		
		if (!DRY_RUN) {
			if (client_date >= 20150513) {	
				var this_ptr = 	new ParameterImpl("this", new Pointer32DataType(), this.getCurrentProgram());
				var a1 = 		new ParameterImpl("a1", new IntegerDataType(), this.getCurrentProgram());
				var ret_var = 	new ReturnParameterImpl(new Pointer32DataType(), this.getCurrentProgram());
				
				UIWindowMgr_MakeWindow_fn.updateFunction("__thiscall", ret_var, 
						FunctionUpdateType.DYNAMIC_STORAGE_ALL_PARAMS, false, 
						SourceType.USER_DEFINED, this_ptr, a1);
			} else {
				printerr("Unable to set function parameters for UIWindowMgr::MakeWindow: Clientdate: " + client_date);
				return "";
			}
		}
		
		/* add static content */
		output += "\n";
		output += "struct UIFrameWnd {};\n";
		output += "typedef UIFrameWnd* (__thiscall* lpMakeWindow)(void*, int);\n";
		output += "\n";
		
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Searching for UIBookWnd__SendMsg
    	//
		
		/* add static content */
		output += "\t// sub types\n";
		output += "\t// CUIBookWnd\n";
		
		
		/* search function */
		Set<Function> func_set = (Set<Function>)pib.string_to_reflist(LISTTYPE.SET, "%sbook\\%s.txt");
		
		Function func_addr = null;
		Function func_addr_tmp = null;
		int counter = 0;
		sig = pib.toSignature("6a 00 6a 00 6a 00 68.{0,4} 6a 06");
		for(Iterator<Function> it = func_set.iterator(); it.hasNext();) {
			func_addr = it.next();
			if (func_addr == null) {
				pib.log("func_addr was null!");
				continue;
			}
			
			var sig_addr_lst = findBytes(func_addr.getBody(), sig, 1, 2, false);
			if (sig_addr_lst.length > 0) {
				pib.log("Func_addr: " + func_addr);
				pib.log("Signature found at: " + sig_addr_lst[0]);
				func_addr_tmp = func_addr;
				counter++;
			}
		}
		
		if (counter > 1) {
			printerr("UIWindowMgr: counter was greater 1");
			return "";
		}
		
		func_addr = func_addr_tmp ;
			
		pib.log("UIBookWnd__SendMsg: " + func_addr.getEntryPoint());
		output += "\t#define UIBOOKWND_SENDMSG_FN 0x" + func_addr.getEntryPoint() + "\n";
		
		/* set function parameters */
		var UIBookWnd_SendMsg_fn = fm.getFunctionContaining(func_addr.getEntryPoint());
		UIBookWnd_SendMsg_fn.setName("UIBookWnd::SendMsg", SourceType.USER_DEFINED);
		
		if (!DRY_RUN) {
			if (client_date >= 20150513) {
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
				return "";
			}
		}
		
		/* add static content */
		output += "\n";
		output += "\ttypedef void(__thiscall* lpSendMsg)(void*, int, int, char*, int, int, int);\n";
		output += "\t//struct CUIBookWnd {\n";
		output += "\t#define UIBOOKWND_DATA		\\\n";
		
		//
		// Searching for book_title location
		//
		sig = pib.toSignature("8d 86.{4} 50 8b ce e8.{4}");
		var tmp_sig_addr = findBytes(UIBookWnd_SendMsg_fn.getBody(), sig, 1, 1, false);
		if (tmp_sig_addr == null || tmp_sig_addr.length != 2) {
			if (tmp_sig_addr != null)
				pib.log("Size: " + tmp_sig_addr.length);
			for (var f : tmp_sig_addr)
				pib.log(f.toString());
			printerr("Book Title signature: Error!");
			return "";
		}
		pib.log("BookTitle signature: " + tmp_sig_addr[0]);
		
		var offset_tmp = getByte(tmp_sig_addr[0].add(2)); // get the first signature
		var book_title_offset = String.format("%02x", offset_tmp);
		pib.log("BookTitle offset: " + book_title_offset);
		
		output += "\t\t/* 0x0	*/ BYTE offset0[0x" + book_title_offset + "];	\\\n";
		output += "\t\t/* 0x" + book_title_offset +"	*/ char book_title[64];\n";
		output += "\t//}\n";
		
		return output;
   	}

	public void run_test() throws Exception {
		DRY_RUN = true; // test are always dry runs!
		if (!test(execute()))
	    	printerr(ANSI_RED + "Test failed!" + ANSI_RESET);
	    else
	    	println(ANSI_GREEN + "Test successful!" + ANSI_RESET);
	}
    
    /* used to test correct output.
     * is based on already known and confirmed outputs.
     */
    public boolean test(String output) throws MemoryAccessException {
    	int client_date = pib.checkDate();
    	
    	/* Test 20150513 client output */
    	if (client_date == 20150513) {
    		String test_str = "\n//\n" + 
    				"// CUIWindowMgr defines\n" + 
    				"//\n" + 
    				"#define UIWINDOWMGR_MAKEWINDOW_FN 0x005e95a0\n" + 
    				"#define UIWINDOWMGR_PTR 0x00c6f600\n" + 
    				"struct UIFrameWnd {};\n" + 
    				"typedef UIFrameWnd* (__thiscall* lpMakeWindow)(void*, int);\n" + 
    				"\n" + 
    				"	// sub types\n" + 
    				"	// CUIBookWnd\n" + 
    				"	#define UIBOOKWND_SENDMSG_FN 0x005199f0\n" + 
    				"\n" + 
    				"	typedef void(__thiscall* lpSendMsg)(void*, int, int, char*, int, int, int);\n" + 
    				"	//struct CUIBookWnd {\n" + 
    				"	#define UIBOOKWND_DATA		\\\n" + 
    				"		/* 0x0	*/ BYTE offset0[0x84];	\\\n" + 
    				"		/* 0x84	*/ char book_title[64];\n" + 
    				"	//}\n";
    		
    		boolean res = output.equals(test_str);
    		if (res)
    			return true;
			print(test_str);
			print(output);
			return false;
    	}
    	
    	printerr("No valid client date for test!");
    	return false;
    }
}
