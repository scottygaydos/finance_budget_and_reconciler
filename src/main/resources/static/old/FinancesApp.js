(function() {
	var app = angular.module("FinancesApp", ['ngRoute', 'customFilters']);
	
	app.config(function($routeProvider) {
		$routeProvider
			.when('/', {
				controller: 'StartPageController',
				templateUrl: 'views/startpage.html'
			})
            .when('/transactions/', {
                controller: 'TransactionsController',
                templateUrl: 'views/transactions.html'
            })
			.when('/createtransaction/', {
				controller: 'CreateTransactionController',
				templateUrl: 'views/createtransaction.html'
			})
			.when('/budget/', {
				controller: 'BudgetController',
				templateUrl: 'views/budget.html'
			})
			.when('/bills/', {
				controller: 'BillsController',
				templateUrl: 'views/bills.html'
			})
			.when('/moneyowed/', {
				controller: 'MoneyOwedController',
				templateUrl: 'views/moneyowed.html'
			})
			.when('/reconcile/', {
				controller: 'ReconcileController',
				templateUrl: 'views/reconcile.html'
			})
			.when('/add_budget_for_month/', {
				controller: 'AddBudgetController',
				templateUrl: 'views/add_budget_for_month.html'
			})
			.otherwise( {
				redirectTo: '/'
			});
	});
	
}());