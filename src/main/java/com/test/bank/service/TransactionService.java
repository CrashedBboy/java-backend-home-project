package com.test.bank.service;

import com.test.bank.initializer.DataSourceInitializer;
import com.test.bank.model.TransferResponse;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DSL;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.test.bank.db.Tables.USER;
import com.test.bank.db.tables.records.UserRecord;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionService {

    DefaultConfiguration jooqConfiguration;

    @Inject
    public TransactionService(DataSourceInitializer dataSourceInitializer) {
        this.jooqConfiguration = dataSourceInitializer.getJooqConfiguration();
    }

    public TransferResponse transfer(int fromUserId, int toUserId, int amount) {
        // implement transfer

        // get each user's wallet
        UserRecord fromUserRecord = DSL.using(jooqConfiguration).fetchOne(USER, USER.ID.eq(UInteger.valueOf(fromUserId)));
        UserRecord toUserRecord = DSL.using(jooqConfiguration).fetchOne(USER, USER.ID.eq(UInteger.valueOf(toUserId)));

        // calculate new wallet amount for both user
        Integer newFromUserWallet = fromUserRecord.getWallet() - amount;
        Integer newToUserWallet = toUserRecord.getWallet() + amount;

        // make the transaction meets ACID properties
        DSL.using(jooqConfiguration).transaction(jooqConfiguration -> {

            DSL.using(jooqConfiguration)
                .update(USER)
                .set(USER.WALLET, newFromUserWallet)
                .where(USER.ID.eq(UInteger.valueOf(fromUserId)))
                .execute();

            DSL.using(jooqConfiguration)
                .update(USER)
                .set(USER.WALLET, newToUserWallet)
                .where(USER.ID.eq(UInteger.valueOf(toUserId)))
                .execute();
        });

        return null;
    }

}
