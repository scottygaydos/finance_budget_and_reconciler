(function() {
    var TransactionsController = function($scope, TransactionFactory) {

        $scope.transactions = null;
        $scope.error = null;


        function init() {
            $scope.transactions = TransactionFactory.getTransactions()
                .success(function(transactions) {
                    $scope.transactions = transactions;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

        }

        init();

    };

    angular.module('FinancesApp').controller('TransactionsController', TransactionsController);
}());