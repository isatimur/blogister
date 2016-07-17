'use strict';

describe('Controller Tests', function() {

    describe('Posts Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPosts, MockTag, MockBlog;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPosts = jasmine.createSpy('MockPosts');
            MockTag = jasmine.createSpy('MockTag');
            MockBlog = jasmine.createSpy('MockBlog');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Posts': MockPosts,
                'Tag': MockTag,
                'Blog': MockBlog
            };
            createController = function() {
                $injector.get('$controller')("PostsDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'blogisterApp:postsUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
