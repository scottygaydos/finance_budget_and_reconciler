(function() {
    var TransactionTypeFactory = function($http) {

        var factory = {};
        factory.getTransactionTypes = function() {
            //return transactions;
            return $http.get('/ws/transaction/types/');
        };

        return factory;
    };

    angular.module('FinancesApp').factory('TransactionTypeFactory', TransactionTypeFactory);

}());