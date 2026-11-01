package com.company.employee.controller;

import com.company.employee.dto.EmployeeRequest;
import com.company.employee.model.Employee;
import com.company.employee.service.EmployeeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    private final EmployeeService service;

    @Inject
    public EmployeeResource(EmployeeService service) {
        this.service = service;
    }

    @POST
    public Response create(@Valid EmployeeRequest req, @Context UriInfo uri) {
        Employee created = service.create(toModel(req));
        URI location = uri.getAbsolutePathBuilder().path(created.getEmployeeId().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Employee get(@PathParam("id") long id) {
        return service.get(id);
    }

    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(service.list(page, size)).build();
    }

    @PUT
    @Path("/{id}")
    public Employee update(@PathParam("id") long id, @Valid EmployeeRequest req) {
        return service.update(id, toModel(req));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    private Employee toModel(EmployeeRequest r) {
        Employee e = new Employee();
        e.setEmployeeNumber(r.employeeNumber);
        e.setFirstName(r.firstName);
        e.setLastName(r.lastName);
        e.setEmail(r.email);
        e.setDepartment(r.department);
        e.setSalary(r.salary);
        e.setStatus(r.status);
        return e;
    }
}
