'use strict';

describe('Controller Tests', function() {

    describe('Tag Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockTag, MockPosts;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockTag = jasmine.createSpy('MockTag');
            MockPosts = jasmine.createSpy('MockPosts');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Tag': MockTag,
                'Posts': MockPosts
            };
            createController = function() {
                $injector.get('$controller')("TagDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'blogisterApp:tagUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
