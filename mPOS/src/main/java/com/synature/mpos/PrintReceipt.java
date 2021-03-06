package com.synature.mpos;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.synature.mpos.database.PrintReceiptLogDao;
import com.synature.util.Logger;

public class PrintReceipt extends AsyncTask<Void, Void, Void>{
	
	public static final String TAG = "PrintReceipt";
	
	private PrintReceiptLogDao mPrintLog;
	private Context mContext;
	
	/**
	 * @param context
	 */
	public PrintReceipt(Context context){
		mContext = context;
		mPrintLog = new PrintReceiptLogDao(context);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		List<PrintReceiptLogDao.PrintReceipt> printLogLst = mPrintLog.listPrintReceiptLog(); 
		for(int i = 0; i < printLogLst.size(); i++){
			PrintReceiptLogDao.PrintReceipt printReceipt = printLogLst.get(i);
			try {
				if(Utils.isInternalPrinterSetting(mContext)){
					WintecPrinter wtPrinter = new WintecPrinter(mContext);
					wtPrinter.createTextForPrintReceipt(printReceipt.getTransactionId(), printReceipt.isCopy(), false);
					wtPrinter.print();
				}else{
					EPSONPrinter epPrinter = new EPSONPrinter(mContext);
					epPrinter.createTextForPrintReceipt(printReceipt.getTransactionId(), printReceipt.isCopy(), false);
					epPrinter.print();
				}
				mPrintLog.deletePrintStatus(printReceipt.getPrintId(), printReceipt.getTransactionId());
				
			} catch (Exception e) {
				mPrintLog.updatePrintStatus(printReceipt.getPrintId(), printReceipt.getTransactionId(), PrintReceiptLogDao.PRINT_NOT_SUCCESS);
				Logger.appendLog(mContext, 
						Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
						" Print receipt fail : " + e.getMessage());
			}
		}
		return null;
	}

}
