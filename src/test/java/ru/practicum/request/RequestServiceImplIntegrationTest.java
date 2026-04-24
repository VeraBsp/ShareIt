package ru.practicum.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestServiceImplIntegrationTest {
    @Autowired
    private RequestService requestService;

    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        requestor = userRepository.save(User.builder()
                .name("Requester")
                .email("req@mail.com")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.com")
                .build());
    }

    @Test
    void create_shouldSaveRequest() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a bike")
                .build();

        ItemRequestDto created = requestService.create(dto, requestor.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(requestRepository.findAll()).hasSize(1);
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build());

        ItemRequestDto result =
                requestService.getById(request.getId(), requestor.getId());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getDescription()).isEqualTo("Need drill");
    }

    @Test
    void getAllByUser_shouldReturnOnlyOwnRequests() {
        ItemRequest own = requestRepository.save(ItemRequest.builder()
                .description("Own request")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Other request")
                .requestor(otherUser)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> result =
                requestService.getAllByUser(requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription())
                .isEqualTo("Own request");
    }

    @Test
    void getAll_shouldReturnOtherUsersRequestsPaged() {
        ItemRequest otherRequest = requestRepository.save(ItemRequest.builder()
                .description("Other user request")
                .requestor(otherUser)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Item for request")
                .description("Desc")
                .available(true)
                .owner(requestor)
                .request(otherRequest)
                .build());

        List<ItemRequestDto> result =
                requestService.getAll(requestor.getId(), 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(1);
    }
}