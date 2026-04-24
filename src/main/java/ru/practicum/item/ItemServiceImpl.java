package ru.practicum.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.BookingShortDto;
import ru.practicum.booking.BookingStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ItemRequest;
import ru.practicum.request.ItemRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository requestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, CommentRepository commentRepository, BookingRepository bookingRepository, ItemRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);
        if (dto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithBookingDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingDto dto = toItemWithBookingDto(item);

        if (item.getOwner().getId().equals(userId)) {
            setBookings(item, dto);
        }

        return dto;
    }

    @Override
    public List<ItemWithBookingDto> getAllByOwner(Long ownerId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return itemRepository.findByOwnerId(ownerId, pageable).stream()
                .map(this::toItemWithBookingDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text, int from, int size) {
        if (text.isBlank()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return itemRepository.search(text.toLowerCase(), pageable).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasBooking = bookingRepository
                .existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        userId,
                        itemId,
                        LocalDateTime.now(),
                        BookingStatus.APPROVED
                );

        if (!hasBooking) {
            throw new BadRequestException("Пользователь не арендовал эту вещь");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentDto.builder()
                .id(saved.getId())
                .text(saved.getText())
                .authorName(saved.getAuthor().getName())
                .created(saved.getCreated())
                .build();
    }

    private void setBookings(Item item, ItemWithBookingDto dto) {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository
                .findLastBooking(item.getId(), now)
                .ifPresent(b -> dto.setLastBooking(
                        new BookingShortDto(b.getId(), b.getBooker().getId())
                ));

        bookingRepository
                .findNextBooking(item.getId(), now)
                .ifPresent(b -> dto.setNextBooking(
                        new BookingShortDto(b.getId(), b.getBooker().getId())
                ));
    }

    private ItemWithBookingDto toItemWithBookingDto(Item item) {
        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(item.getId())
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .text(c.getText())
                        .authorName(c.getAuthor().getName())
                        .created(c.getCreated())
                        .build())
                .toList();

        return ItemWithBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(comments)
                .build();
    }
}
