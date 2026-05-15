package ru.practicum.shareit.server.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.dto.BookingShortDto;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.BookingStatus;
import ru.practicum.shareit.server.exception.BadRequestException;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

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

        ItemRequest request = null;
        if (dto.getRequestId() != null) {
            request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        Item item = ItemMapper.toItem(dto, owner, request);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
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
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        return itemRepository.findByOwnerId(ownerId, pageable)
                .stream()
                .map(item -> {
                    ItemWithBookingDto dto = toItemWithBookingDto(item);
                    setBookings(item, dto);
                    return dto;
                })
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
                .findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(item.getId(), now, BookingStatus.APPROVED)
                .ifPresent(b -> dto.setLastBooking(
                        new BookingShortDto(b.getId(), b.getBooker().getId())
                ));

        bookingRepository
                .findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(item.getId(), now, BookingStatus.APPROVED)
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
