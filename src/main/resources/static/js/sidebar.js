angular
    .module('MyApp', ['ngSanitize', 'ngMaterial', 'ngMessages', 'material.svgAssetsCache', 'ngRoute', 'ngAnimate'])
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider
            .when("/create", {
                templateUrl: "/create",
                controller: 'AppCtrl',
                resolve: {
                    patterns: function ($q,$http,$route) {
                        var defer = $q.defer();
                        $http.get('/patterns').then(function (response) {
                            console.log(response.data);
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    }
                }
            })
            .when("/stands/:page", {
                templateUrl: "/content",
                controller: 'AppCtrl',
                resolve: {
                    stands: function ($q, $http, $route) {
                        var defer = $q.defer();
                        $http.get('/stands?page=' + $route.current.params.page).then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    }
                }
            })
            .when("/", {
                templateUrl: "/content",
                controller: 'AppCtrl',
                resolve: {
                    stands: function ($q, $http) {
                        var defer = $q.defer();
                        $http.get('/stands').then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    }
                }
            })
            .when("/stand/:id", {
                templateUrl : "/stand",
                controller: 'AppCtrl',
                resolve: {
                    stand: function ($q,$http,$route) {
                        var defer = $q.defer();
                        $http.get('/'+$route.current.params.id).then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    },
                    canManage: function ($q,$http,$route) {
                        var defer = $q.defer();
                        $http.get('/canManageStand/'+$route.current.params.id).then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    }
                }
            })
            .when("stand/:id/update", {
                templateUrl : "/create",
                controller: 'AppCtrl',
                resolve: {
                    stand: function ($q,$http,$route) {
                        var defer = $q.defer();
                        $http.get('/'+$route.current.params.id+'/update').then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    },
                    canManage: function ($q,$http,$route) {
                        var defer = $q.defer();
                        $http.get('/canManageStand/'+$route.current.params.id).then(function (response) {
                            defer.resolve(response.data);
                        });
                        return defer.promise;
                    }
                }

            })
            .otherwise({
                redirectTo: '/'
            });
        // $locationProvider.html5Mode(true);
    })

    .controller('AppCtrl', function ($scope, $timeout, $mdSidenav, $log,$window,$http,$sce) {
        $scope.deliberatelyTrustDangerousSnippet = function(body) {
            return $sce.trustAsHtml(body);
        };
        angular.element(document).ready(function () {
            CKEDITOR.replace('text');
            CKEDITOR.config.codeSnippet_theme = 'idea';
        });
        $scope.toggleLeft = buildDelayedToggler('left');
        $scope.toggleRight = buildToggler('right');
        $scope.isOpenRight = function () {
            return $mdSidenav('right').isOpen();
        };
        $scope.thisPage = function (pageId) {
            $window.location.href = '/#/stands/'+pageId;
        };
        $scope.range = function(min, max){
            var input = [];
            for (var i = min; i <= max; i ++) input.push(i);
            return input;
        };
        $scope.delete = function (standId) {
            $http.get('/'+standId+'/delete').then(function () {
                $window.location.href = '/#/';
            })
        };

        $scope.back = function () {
            history.back();
        };

        $scope.showReg = false;
        $scope.showLogin = true;

        $scope.hideLogin = function () {
            $timeout(function () {
                $scope.showReg = true;
            }, 450);
            $scope.showLogin= false;

        };

        $scope.hideReg = function () {
            $scope.showReg = false;
            $timeout(function () {
                $scope.showLogin= true;
            }, 450);
        };
        /**
         * Supplies a function that will continue to operate until the
         * time is up.
         */
        function debounce(func, wait, context) {
            var timer;

            return function debounced() {
                var context = $scope,
                    args = Array.prototype.slice.call(arguments);
                $timeout.cancel(timer);
                timer = $timeout(function () {
                    timer = undefined;
                    func.apply(context, args);
                }, wait || 10);
            };
        }

        /**
         * Build handler to open/close a SideNav; when animation finishes
         * report completion in console
         */
        function buildDelayedToggler(navID) {
            return debounce(function () {
                // Component lookup should always be available since we are not using `ng-if`
                $mdSidenav(navID)
                    .toggle()
                    .then(function () {
                        $log.debug("toggle " + navID + " is done");
                    });
            }, 200);
        }

        function buildToggler(navID) {
            return function () {
                // Component lookup should always be available since we are not using `ng-if`
                $mdSidenav(navID)
                    .toggle()
                    .then(function () {
                        $log.debug("toggle " + navID + " is done");
                    });
            };
        }
        // $scope.currentPage = 0;
        //
        // $scope.paging = {
        //     total: 10,
        //     current: 1,
        //     onPageChanged: loadPages
        // };
        //
        // function loadPages() {
        //
        //     $window.location.href = '/#/stands/'+$scope.paging.current;
        //
        //     $scope.currentPage = $scope.paging.current;
        // }
    })
    .controller('LeftCtrl', function ($scope, $timeout, $mdSidenav, $log) {
        $scope.close = function () {
            // Component lookup should always be available since we are not using `ng-if`
            $mdSidenav('left').close()
                .then(function () {
                    $log.debug("close LEFT is done");
                });

        };
    })
    .controller('RightCtrl', function ($scope, $timeout, $mdSidenav, $log) {
        $scope.close = function () {
            // Component lookup should always be available since we are not using `ng-if`
            $mdSidenav('right').close()
                .then(function () {
                    $log.debug("close RIGHT is done");
                });
        };
    });
