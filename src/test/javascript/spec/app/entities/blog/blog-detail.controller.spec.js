'use strict';

describe('Controller Tests', function() {

    describe('Blog Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockBlog, MockUser, MockPosts;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockBlog = jasmine.createSpy('MockBlog');
            MockUser = jasmine.createSpy('MockUser');
            MockPosts = jasmine.createSpy('MockPosts');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Blog': MockBlog,
                'User': MockUser,
                'Posts': MockPosts
            };
            createController = function() {
                $injector.get('$controller')("BlogDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'blogisterApp:blogUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
