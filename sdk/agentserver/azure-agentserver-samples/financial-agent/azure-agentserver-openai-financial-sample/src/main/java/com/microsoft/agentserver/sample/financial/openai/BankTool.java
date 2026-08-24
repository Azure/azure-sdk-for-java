// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import java.util.HashMap;
import java.util.Map;

/**
 * A tiny in-memory bank used by the sample's tools. Unlike the langchain4j
 * financial sample, this class carries no framework annotations — the tool
 * schema and dispatch are declared explicitly in {@link FinancialToolbox}.
 */
public class BankTool {

    private final Map<String, Double> accounts = new HashMap<>();

    /**
     * Seeds an account with a starting balance.
     *
     * @param user           the account holder's name
     * @param initialBalance the starting balance
     */
    public void createAccount(String user, Double initialBalance) {
        if (accounts.containsKey(user)) {
            throw new IllegalArgumentException("Account for user " + user + " already exists");
        }
        accounts.put(user, initialBalance);
    }

    /**
     * Returns the balance for the given user.
     *
     * @param user the account holder's name
     * @return the current balance
     */
    public double getBalance(String user) {
        Double balance = accounts.get(user);
        if (balance == null) {
            throw new IllegalArgumentException("No account found for user " + user);
        }
        return balance;
    }

    /**
     * Credits the user by the given amount and returns the new balance.
     *
     * @param user   the account holder's name
     * @param amount the amount to add
     * @return the new balance
     */
    public double credit(String user, double amount) {
        double newBalance = getBalance(user) + amount;
        accounts.put(user, newBalance);
        return newBalance;
    }

    /**
     * Withdraws the given amount from the user and returns the new balance.
     *
     * @param user   the account holder's name
     * @param amount the amount to remove
     * @return the new balance
     */
    public double withdraw(String user, double amount) {
        double newBalance = getBalance(user) - amount;
        accounts.put(user, newBalance);
        return newBalance;
    }
}
