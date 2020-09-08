(function() {
    var CreateTransactionController = function($scope, TransactionTypeFactory, AccountFactory, TransactionFactory, $location, $filter) {

        $scope.submit = function() {
            var canReconcile = "0";
            if ($scope.selectedTxCanReconcile) {
                canReconcile = "1";
            }

            $scope.createTransactionResult = TransactionFactory.createTransaction(
                    $filter("date")($scope.selectedTxDate, "yyyy-MM-dd"),
                    $scope.selectedCreditAccountId,
                    $scope.selectedDebitAccountId,
                    $scope.selectedTransactionTypeId,
                    $scope.selectedTxDescription,
                    $scope.selectedTxAuthAmount,
                    $scope.selectedTxSettledAmount,
                    canReconcile)
                .then(
                function(response) {
                    if (response.data.resultCode == 100) {
                        $location.path('/transactions/');
                    } else {
                        alert('Create tx failed: '.concat(response.data.message));
                    }
                }, function (loginResult, status, headers, config) {
                    $scope.error = "Could not get data";
                }
            );
        }

        function init() {
            $scope.selectedTxDate = new Date();

            $scope.transactionTypes = TransactionTypeFactory.getTransactionTypes()
                .success(function(transactionTypes) {
                    $scope.transactionTypes = transactionTypes;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

            $scope.creditableAccounts = AccountFactory.getCreditableAccounts()
                .success(function(creditableAccounts) {
                    $scope.creditableAccounts = creditableAccounts;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

            $scope.creditableAccount = $scope.creditableAccounts[1];

            $scope.debitableAccounts = AccountFactory.getDebitableAccounts()
                .success(function(debitableAccounts) {
                    $scope.debitableAccounts = debitableAccounts;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });
            $scope.selectedTxCanReconcile = true;
        }

        init();

    };

    angular.module('FinancesApp').controller('CreateTransactionController', CreateTransactionController);
}());