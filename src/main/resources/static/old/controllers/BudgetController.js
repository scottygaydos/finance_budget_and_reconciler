(function() {
    var BudgetController = function($scope, BudgetFactory, $log, TransactionTypeFactory) {

        $scope.budgetObject = null;
        $scope.error = null;
        $scope.budgetProblem = false;
        $scope.currentMonth = new Date().getMonth()+1;

        var budgetMonth = $scope.budgetMonth;
        if (budgetMonth == null) {
            budgetMonth = new Date().getMonth()+1;
            $scope.budgetMonth = budgetMonth;
        }

        var budgetYear = $scope.budgetYear;
        if (budgetYear == null) {
            budgetYear = new Date().getFullYear();
            $scope.budgetYear = budgetYear;
        }

        $scope.submit = function() {
            $scope.budgetObject = BudgetFactory.reportBudget($scope.budgetMonth, $scope.budgetYear)
                .success(function(budgetObject) {
                    $scope.budgetObject = budgetObject;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });
        }

        $scope.fixBudgetToMatchDeposits = function() {
            BudgetFactory.fixBudgetToMatchDeposits(
                $scope.budgetMonth,
                $scope.budgetYear,
                $scope.selectedTransactionTypeId,
                $scope.budgetObject.discrepancyBetweenTotalBudgetAndPaychecks)
                .then(function() {init($scope.budgetMonth, $scope.budgetYear)}); // chain 'then' to make init wait for resolution.
        }

        $scope.moveBudgetForward = function() {
            BudgetFactory.moveBudgetToNextMonth(
                $scope.budgetMonth,
                $scope.budgetYear,
                $scope.budgetObject.totalRemainingBudget)
                .then(function() {init($scope.budgetMonth, $scope.budgetYear)}); // chain 'then' to make init wait for resolution.;
        }


        function init(budgetMonth, budgetYear) {
            $scope.budgetObject = BudgetFactory.reportBudget(budgetMonth, budgetYear)
                .success(function(budgetObject) {
                    $scope.budgetObject = budgetObject;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

            $scope.transactionTypes = TransactionTypeFactory.getTransactionTypes()
                .success(function(transactionTypes) {
                    $scope.transactionTypes = transactionTypes;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

        }

        init(budgetMonth, budgetYear);

    };

    angular.module('FinancesApp').controller('BudgetController', BudgetController);
}());