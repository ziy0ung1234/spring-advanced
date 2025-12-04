package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserChangePasswordRequest userChangePasswordRequest;

    @Test
    void 유저_조회_성공_테스트() {
        //given
        long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        //when
        UserResponse userResponse = userService.getUser(userId);
        //then
        assertThat(userResponse.getId()).isEqualTo(userId);
        assertThat(userResponse.getEmail()).isEqualTo("test@test.com");
    }
    @Test
    void 유저_없으면_예외_발생() {
        // given
        long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() ->
                userService.getUser(userId)
        ).isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");
    }
    @Test
    void 비밀번호_변경_성공() {
        //given
        long userId = 1L;
        String oldPassword = "Oldpassword123";
        String newPassword = "Newpassword456";
        String encodedOldPassword = "encodedOldPassword123";
        User user = new User("test@test.com", encodedOldPassword, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserChangePasswordRequest request = new UserChangePasswordRequest();
        ReflectionTestUtils.setField(request, "newPassword", newPassword);
        ReflectionTestUtils.setField(request, "oldPassword", oldPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        //현재 비밀번호 검증 -> 성공해야 함
        given(passwordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(true);

        //새 비밀번호 = 기존 비밀번호인지-> 아니어야 함(false)
        given(passwordEncoder.matches(newPassword, encodedOldPassword)).willReturn(false);

        //새 비밀번호 인코딩
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPassword123");

        //when
        userService.changePassword(userId, request);

        //then
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword123");
    }

    @Test
    void 새_비밀번호가_기존과_같으면_예외_발생() {
        // given
        long userId = 1L;
        String oldPassword = "Oldpassword123";
        String newPassword = "Newpassword456";
        String encodedOldPassword = "encodedOldPassword123";
        User user = new User("test@test.com", encodedOldPassword, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserChangePasswordRequest request = new UserChangePasswordRequest();
        ReflectionTestUtils.setField(request, "newPassword", newPassword);
        ReflectionTestUtils.setField(request, "oldPassword", oldPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(passwordEncoder.matches(newPassword, encodedOldPassword)).willReturn(true);

        // when & then
        assertThatThrownBy(() ->
                userService.changePassword(userId, request)
        ).isInstanceOf(InvalidRequestException.class)
                .hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

}