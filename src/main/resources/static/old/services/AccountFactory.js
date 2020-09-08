(function() {
    var AccountFactory = function($http, $log) {

        var factory = {};
        factory.getCreditableAccounts = function() {
            //return transactions;
            //$log.log($http.get('/ws/accounts/creditable_accounts/'));
            return $http.get('/ws/accounts/creditable_accounts/');
        };

        factory.getDebitableAccounts = function() {
            //return transactions;
            return $http.get('/ws/accounts/debitable_accounts/');
        };

        return factory;
    };

    angular.module('FinancesApp').factory('AccountFactory', AccountFactory);

}());