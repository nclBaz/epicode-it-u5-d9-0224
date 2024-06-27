package riccardogulin.u5d9.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import riccardogulin.u5d9.entities.User;
import riccardogulin.u5d9.exceptions.BadRequestException;
import riccardogulin.u5d9.payloads.NewUserDTO;
import riccardogulin.u5d9.payloads.NewUserResponseDTO;
import riccardogulin.u5d9.services.UsersService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {
	@Autowired
	private UsersService usersService;

	// 1 . GET http://localhost:3001/users
	@GetMapping
	public Page<User> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy) {
		return this.usersService.getUsers(page, size, sortBy);
	}

	// 2 . POST http://localhost:3001/users
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public NewUserResponseDTO saveUser(@RequestBody @Validated NewUserDTO body, BindingResult validationResult) {
		// @Validated serve per triggerare la validazione del payload
		// Tramite questo BindingResult possiamo valutare se la validazione ha prodotto degli errori ed in tal caso
		// triggerare un errore 400 Bad Request, magari anche contenente la lista degli errori come messaggio
		if (validationResult.hasErrors()) { // Se ci sono stati errori di validazione, lanciamo il 400
			System.out.println(validationResult.getAllErrors());
			throw new BadRequestException(validationResult.getAllErrors());
		}
		return new NewUserResponseDTO(this.usersService.save(body).getId());
	}

	// 3. GET http://localhost:3001/users/{userId}
	@GetMapping("/{userId}")
	public User findById(@PathVariable UUID userId) {
		return this.usersService.findById(userId);
	}

	// 4. PUT http://localhost:3001/users/{userId}
	@PutMapping("/{userId}")
	public User findByIdAndUpdate(@PathVariable UUID userId, @RequestBody User body) {
		return this.usersService.findByIdAndUpdate(userId, body);
	}


	// 5. DELETE http://localhost:3001/users/{userId}
	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void findByIdAndDelete(@PathVariable UUID userId) {
		this.usersService.findByIdAndDelete(userId);
	}

	@PostMapping("/{userId}/avatar")
	public String uploadAvatar(@RequestParam("avatar") MultipartFile image) throws IOException {
		// il request param "avatar" deve corrispondere ESATTAMENTE alla chiave del payload Multipart dove stiamo
		// allegando il file. Se non corrispondono il file non verrà trovato

		return this.usersService.uploadImage(image);
	}

}
