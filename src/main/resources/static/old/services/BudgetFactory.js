(function() {
    var BudgetFactory = function($http) {

        var factory = {};
        factory.reportBudget = function(budgetMonth, budgetYear) {
            return $http.get('/ws/budget/report/', {
                params: {budget_month: budgetMonth, budget_year: budgetYear}
            });

        };

        factory.reportBudgetTemplate = function() {
            return $http.get('/ws/budget/template_report/', {
                params: {}
            });
        };

        factory.createBudgetForMonth = function(budgetMonth, budgetYear) {
            return $http.post(
                '/ws/budget/create_for_month/',
                $.param({budget_month: budgetMonth,
                    budget_year: budgetYear}),
                {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
            );
        };

        factory.fixBudgetToMatchDeposits = function(budgetMonth, budgetYear, transactionTypeId, diffAmount) {
            return $http.post(
                '/ws/budget/fix_month_to_match_deposits/',
                $.param({budget_month: budgetMonth,
                    budget_year: budgetYear,
                    transaction_type_id: transactionTypeId,
                    diff_amount: diffAmount}),
                {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
            );
        };

        factory.moveBudgetToNextMonth = function(budgetMonth, budgetYear, amount) {
            return $http.post(
                '/ws/budget/move_remainder_to_next_month/',
                $.param({budget_month: budgetMonth,
                    budget_year: budgetYear,
                    amount: amount}),
                {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
            );
        };

        return factory;
    };

    angular.module('FinancesApp').factory('BudgetFactory', BudgetFactory);

}());