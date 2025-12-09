package de.treestack.auth;

import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class DomainRoleIdpMapperTest {

    @Test
    void when_callingUpdateBrokeredUser_expect_delegateToAssignRole() {
        // Arrange
        UserModel user = mock(UserModel.class);
        when(user.getEmail()).thenReturn("nobody@example.org");
        DomainRoleIdpMapper mapper = Mockito.spy(new DomainRoleIdpMapper());

        // Act
        mapper.updateBrokeredUser(mock(), mock(), user, mock(), mock());

        // Assert
        // Can only verify side effects, as assignRole is static
        verify(user).getEmail();
    }

}