(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('posts', {
            parent: 'entity',
            url: '/posts?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'blogisterApp.posts.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/posts/posts.html',
                    controller: 'PostsController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('posts');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('posts-detail', {
            parent: 'entity',
            url: '/posts/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'blogisterApp.posts.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/posts/posts-detail.html',
                    controller: 'PostsDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('posts');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Posts', function($stateParams, Posts) {
                    return Posts.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('posts.new', {
            parent: 'posts',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/posts/posts-dialog.html',
                    controller: 'PostsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                title: null,
                                content: null,
                                creattionDate: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('posts', null, { reload: true });
                }, function() {
                    $state.go('posts');
                });
            }]
        })
        .state('posts.edit', {
            parent: 'posts',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/posts/posts-dialog.html',
                    controller: 'PostsDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Posts', function(Posts) {
                            return Posts.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('posts', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('posts.delete', {
            parent: 'posts',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/posts/posts-delete-dialog.html',
                    controller: 'PostsDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Posts', function(Posts) {
                            return Posts.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('posts', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
