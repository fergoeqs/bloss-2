//package org.fergoeqs.blps1.config;
//
//import com.atomikos.icatch.jta.UserTransactionManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.transaction.jta.JtaTransactionManager;
//
//import jakarta.transaction.TransactionManager;
//import jakarta.transaction.UserTransaction;
//
//@Configuration
//@EnableTransactionManagement
//public class TransactionConfig {
//
//    @Bean(initMethod = "init", destroyMethod = "close")
//    public UserTransactionManager atomikosTransactionManager() {
//        UserTransactionManager manager = new UserTransactionManager();
//        manager.setTransactionTimeout(300);
//        manager.setForceShutdown(false);
//        return manager;
//    }
//
//    @Bean
//    public JtaTransactionManager transactionManager(
//            UserTransactionManager atomikosTransactionManager) {
//
//        UserTransaction userTransaction = atomikosTransactionManager;
//        TransactionManager transactionManager = atomikosTransactionManager;
//
//        JtaTransactionManager jtaManager = new JtaTransactionManager();
//        jtaManager.setUserTransaction(userTransaction);
//        jtaManager.setTransactionManager(transactionManager);
//        return jtaManager;
//    }
//}