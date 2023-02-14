package com.project.stress_traffic_system.members.service;

import com.project.stress_traffic_system.cart.model.Cart;
import com.project.stress_traffic_system.cart.repository.CartRepository;
import com.project.stress_traffic_system.jwt.JwtUtil;
import com.project.stress_traffic_system.members.dto.MembersRequestDto;
import com.project.stress_traffic_system.members.dto.MembersResponseMsgDto;
import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.entity.MembersRoleEnum;
import com.project.stress_traffic_system.members.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembersService {
    private final MembersRepository membersRepository;
    private final CartRepository cartRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

//    @PostConstruct
    public void init() {
        Members members1 = new Members("test1", "11", MembersRoleEnum.MEMBER);
        Members members2 = new Members("test2", "11",MembersRoleEnum.MEMBER);
        Members members3 = new Members("test3", "11",MembersRoleEnum.MEMBER);

        membersRepository.save(members1);
        membersRepository.save(members2);
        membersRepository.save(members3);
    }

    //클라이언트 상태코드 수정을 위해 사용함
    public void membersExceptionHandler(HttpServletResponse response, int statusCode) {
        //setStatus를 통해 response의 상태 코드 set
        response.setStatus(statusCode);
        //콘텐츠 유형을 json으로 바꿔줌
        response.setContentType("application/json");
    }

    //위 핸들러를 쓰고 dto로 리턴하기
    private MembersResponseMsgDto handleMemberException(String message, HttpStatus status, HttpServletResponse response) {
        membersExceptionHandler(response, status.value());
        return new MembersResponseMsgDto(message, status.value());
    }

    @Transactional
    public MembersResponseMsgDto signup(MembersRequestDto membersRequestDto, HttpServletResponse response) {
        String username = membersRequestDto.getUsername();
        String password = passwordEncoder.encode(membersRequestDto.getPassword());

        //회원 중복 확인
        Optional<Members> found = membersRepository.findByUsername(username);
        if (found.isPresent()) {
            return handleMemberException("중복된 사용자가 존재합니다", HttpStatus.BAD_REQUEST, response);
        }

        //아이디 양식 확인
        if (!membersRequestDto.getUsername().matches("^[a-zA-Z0-9]{5,10}$")) {
            return handleMemberException("아이디 양식을 지켜주세요!", HttpStatus.BAD_REQUEST, response);
        }

        //비밀번호 양식
        if (!membersRequestDto.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,12}$")) {
            return handleMemberException("비밀번호는 영어 대소문자, 숫자의 최소 8자에서 최대 12자리여야 합니다.", HttpStatus.BAD_REQUEST, response);
        }

        MembersRoleEnum role = MembersRoleEnum.MEMBER;

        Members members = new Members(username, password, role);
        Members savedMember = membersRepository.save(members);

        Cart cart = new Cart(savedMember);
        cartRepository.save(cart);

        return handleMemberException("회원가입 성공", HttpStatus.OK, response);
    }

    public MembersResponseMsgDto login(MembersRequestDto membersRequestDto, HttpServletResponse response) {
        String username = membersRequestDto.getUsername();
        String password = membersRequestDto.getPassword();

        //등록된 사용자인지 확인
        Members existMember = membersRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록되지 않은 회원입니다."));

        //비밀번호 확인
//        if (!passwordEncoder.matches(password, existMember.getPassword())) {
//            return handleMemberException("비밀번호가 일치하지 않습니다.",HttpStatus.BAD_REQUEST, response);
//        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(existMember.getUsername(),existMember.getRole()));


        return handleMemberException("로그인 성공", HttpStatus.OK, response);
    }

}
