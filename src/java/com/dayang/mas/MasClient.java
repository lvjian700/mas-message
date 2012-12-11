package com.dayang.mas;

import static com.dayang.mas.utils.CmdUtils.parseCmd;
import static com.dayang.mas.utils.CmdUtils.printHelp;
import static com.dayang.mas.utils.MasUtils.getSmsApiClient;
import static com.dayang.mas.utils.MasUtils.isConnected;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.jasson.mas.api.sms.SmsApiClient;
import com.jasson.mas.api.sms.SmsApiClientHandler;
import com.jasson.mas.api.smsapi.MsgFmt;
import com.jasson.mas.api.smsapi.SmsSendRequest;
import com.jasson.mas.api.smsapi.SmsSendResponse;
import com.jasson.mas.api.smsapi.SmsType;



public class MasClient {
	
	static SmsApiClient smsApiClient = null;
	
	// SmsApiClientHandlerImpl是由自己实现的
	static SmsApiClientHandler smsHandler = new SmsApiClientHandlerImpl();
	
	public static void main(String[] args) throws ParseException {
		CommandLine cl = parseCmd(args);
		
		if(cl.hasOption("h")) {
			printHelp();
			System.exit(1);
			return;
		}
		
		String ip = cl.getOptionValue("ip");
		String strPort = cl.getOptionValue("port");
		int port = Integer.parseInt(strPort);
		
		String appId = cl.getOptionValue("app");
		String pwd = cl.getOptionValue("pwd", "");
		
		String mobile = cl.getOptionValue("mobile");
		if(mobile == null) {
			System.out.println("mobile is required.");
			System.exit(400);			
			return;
		}
		String[] mobiles = mobile.split(",");
		
		String message = cl.getOptionValue("msg", "");
		String xcode = cl.getOptionValue("code", "1111111");
		String strPriority = cl.getOptionValue("priority", "1");
		int priority = Integer.parseInt(strPriority);
		
		try {
			smsApiClient = getSmsApiClient(smsHandler, ip, port,
					appId, pwd);
			
			// 获取网关连接状态(Connect:连接正常, Disconnect:断连, NotConnect:没有连接, Other:其他)
			if(!isConnected(smsApiClient)) {
				System.out.println("网关未连接");
				System.exit(404);
				return;
			}
			
			// 发送短信
			// ======构造发送短信对象开始,下面代码演示发送短信对象几个比较主要的属性值,
			// 其它的属性可以不设置,如果要设置可以参考sendSms方法中 SmsSendRequest参数===
			int ret = smsApiClient.getDestAddrsLimit();
			
			List<String> list = new ArrayList<String>();
			// 每次群发数量不能超过最大限制数
			for (int i = 0; i < ret && i < mobiles.length; i++) {
				String address = mobiles[i];
				list.add(address);
			}
			
			SmsSendRequest smsSendRequest = buildRequest(appId, message, xcode,
					priority, list);
			
			
			SmsSendResponse smsSendResponse = smsApiClient
					.sendSms(smsSendRequest);
			System.out.println("提交成功,requestID:" + smsSendResponse.requestID);
			
		} catch (Exception e) {
			
			System.out.println("API短信客户端调用失败:" + e.getMessage());
			System.exit(500);
			return;
		}
	}

	private static SmsSendRequest buildRequest(String appId, String message,
			String xcode, int priority, List<String> list) {
		SmsSendRequest smsSendRequest = new SmsSendRequest();
		
		smsSendRequest.destAddrs = list;
		smsSendRequest.validTime = 10000; // 短信存活期,单位秒
		smsSendRequest.xCode = xcode; // 短信扩展码
		smsSendRequest.message = message; // 短信内容
		smsSendRequest.msgFormat = MsgFmt.GB2312; // 短信编码类型
		smsSendRequest.isNeedReport = true; // 短信是否需要状态报告
		smsSendRequest.priority = priority; // 短信网关优先级, 短信优先级大于0 的整数
										// 0为最高优先级,数字越大级别越低
		// Normal: 普通短信,Instant:免提短信, Long:长短信,Structured:二进制短信,WapPush:
		// WapPush 短信
		smsSendRequest.type = SmsType.Normal;
		smsSendRequest.appID = appId;
		return smsSendRequest;
	}
	
	
}
