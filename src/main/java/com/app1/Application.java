package com.app1;

import com.alibaba.fastjson.JSON;
import com.bitcode.agent.ProofOfWork;
import com.bitcode.agent.wallet.Wallet;
import com.bitcode.agent.wallet.WalletManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
@ComponentScan("com.bitcode")
public class Application {

    public static void main(String[] args) {

//        List<Integer> test = new LinkedList<>();
//        test.add(0);
//        test.add(1);
//        test.add(2);
//        test.add(3);
//        test.add(4);
//        for(int i: test) {
//            System.out.println(i);
//        }
//
//        System.out.println(JSON.toJSONString(test));
//        test.subList(2,test.size()).clear();
//        System.out.println(JSON.toJSONString(test));
//
//
//        System.exit(-1);

        ProofOfWork.mDiffcult = 21;

        String walletName = "wallet.data";
        if(args.length>1) {
            walletName = args[1];
        };
        String password = "";
        if(args.length>2) {
            password = args[2];
        }



        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        WalletManager walletManager = ctx.getBean("walletManager", WalletManager.class);

        try {
            Wallet wallet = walletManager.load(walletName, password);



        }catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}