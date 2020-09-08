(function() {
    var MoneyOwedFactory = function($http) {

        var factory = {};
        factory.reportMoneyOwedTotals = function() {
            return $http.get('/ws/moneyowed/report/');
        };

        return factory;
    };

    angular.module('FinancesApp').factory('MoneyOwedFactory', MoneyOwedFactory);

}());