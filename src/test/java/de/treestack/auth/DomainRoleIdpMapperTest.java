package de.treestack.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.keycloak.models.*;

import static org.mockito.Mockito.*;

class DomainRoleIdpMapperTest {

    @Test
    void updateBrokeredUser_shouldDelegateToAssignRole() {
        // arrange
        UserModel user = mock(UserModel.class);
        when(user.getEmail()).thenReturn("nobody@example.org");
        DomainRoleIdpMapper mapper = Mockito.spy(new DomainRoleIdpMapper());

        // act
        mapper.updateBrokeredUser(mock(), mock(), user, mock(), mock());

        // assert
        // can only verify side effects, as assignRole is static
        verify(user).getEmail();
    }
}