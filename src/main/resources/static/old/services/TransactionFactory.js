(function() {
    var TransactionFactory = function($http) {

        var factory = {};
        factory.getTransactions = function() {
            //return transactions;
            return $http.get('/ws/transaction/report/');
        };

        factory.createTransaction = function(txDate, creditAccountId, debitAccountId, transactionTypeId, description, authAmt, settAmt, canReconcile) {
            return $http.post(
                '/ws/transaction/create/',
                $.param({transaction_date: txDate,
                            credit_account_id: creditAccountId,
                            debit_account_id: debitAccountId,
                            transaction_type_id: transactionTypeId,
                            description: description,
                            authorized_amount: authAmt,
                            settled_amount: settAmt,
                            can_reconcile: canReconcile}),
                {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
            );
        }

        return factory;
    };

    angular.module('FinancesApp').factory('TransactionFactory', TransactionFactory);

}());